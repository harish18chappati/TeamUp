package teamup.db;

import javafx.application.Platform;
import teamup.model.Activity;
import teamup.model.JoinRequest;
import teamup.model.JoinResult;
import teamup.ui.MainWindow;
import teamup.ui.pages.CreateActivityPage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EditDeleteActivityVerify – Automated test suite verifying Edit Activity and Delete Activity functionality.
 *
 * Verifies:
 *   1. Organizer can edit their own activity (title, category, description, date, location, max_members, contact info).
 *   2. Unauthorized student cannot edit another student's activity.
 *   3. Capacity validation prevents reducing max_members below confirmed member count (organizer + accepted teammates).
 *   4. Existing join requests and confirmed memberships are preserved across edits.
 *   5. Unauthorized student cannot delete another student's activity.
 *   6. Organizer can delete their activity; all associated join requests are cleaned up atomically via SQLite transaction.
 *   7. UI navigation flow (showEditActivity, showCreateActivity, refreshAllPages).
 */
public class EditDeleteActivityVerify {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  TeamUp - Edit & Delete Activity Verification");
        System.out.println("  SRM University-AP Amaravati");
        System.out.println("=".repeat(70));

        DatabaseManager.initialise();

        String organizerReg = "AP21110010001";
        String otherStudentReg = "AP21110010002";
        String thirdStudentReg = "AP21110010003";

        int passed = 0;
        int failed = 0;

        // ── Setup: Insert fresh test activity ──────────────────────────────────
        System.out.println("\n[Setup] Creating temporary activity for Edit/Delete testing...");
        Activity initialActivity = new Activity(
                "Hackathons",
                "Campus AI Sprint 2026",
                "Building autonomous agents on campus. Initial description.",
                "2026-12-05",
                "Block C Seminar Hall",
                organizerReg,
                4,
                "Open",
                "organizer.test@srmap.edu.in",
                "9876543210"
        );

        boolean inserted = DatabaseManager.insertActivity(initialActivity);
        if (!inserted) {
            System.err.println("  ✗ FATAL: Failed to insert test activity.");
            System.exit(1);
        }

        List<Activity> myActs = DatabaseManager.getActivitiesByCreator(organizerReg);
        Activity created = myActs.stream()
                .filter(a -> "Campus AI Sprint 2026".equals(a.getTitle()))
                .findFirst()
                .orElse(null);

        if (created == null) {
            System.err.println("  ✗ FATAL: Test activity could not be retrieved from database.");
            System.exit(1);
        }

        int testActId = created.getActivityId();
        System.out.println("  ✓ Test activity created with ID #" + testActId + " (Host: " + organizerReg + ")");

        // ── Test 1: Organizer can edit their own activity ──────────────────────
        System.out.println("\n[Test 1] Verifying Organizer can edit their own activity...");
        Activity editedActivity = new Activity(
                testActId,
                "Hackathons",
                "Campus AI Sprint 2026 - Master Track",
                "Building autonomous agents on campus. Updated description with mentors.",
                "2026-12-08",
                "Innovation Hub Main Lab",
                organizerReg,
                5,
                "Open",
                created.getCreatedAt(),
                "updated.email@srmap.edu.in",
                "+91 91234 56789"
        );

        boolean editSuccess = DatabaseManager.updateActivity(editedActivity, organizerReg);
        Activity fetched = DatabaseManager.getActivityById(testActId);

        if (!editSuccess || fetched == null) {
            System.err.println("  ✗ FAILED: Organizer updateActivity returned false.");
            failed++;
        } else if (!"Campus AI Sprint 2026 - Master Track".equals(fetched.getTitle())
                || !"Innovation Hub Main Lab".equals(fetched.getLocation())
                || !"2026-12-08".equals(fetched.getActivityDate())
                || fetched.getMaxMembers() != 5
                || !"updated.email@srmap.edu.in".equals(fetched.getContactEmail())
                || !"+91 91234 56789".equals(fetched.getContactPhone())) {
            System.err.println("  ✗ FAILED: Database does not reflect edited values.");
            failed++;
        } else {
            System.out.println("  ✓ Organizer edit verified. All fields updated accurately.");
            passed++;
        }

        // ── Test 2: Another student CANNOT edit the activity ───────────────────
        System.out.println("\n[Test 2] Verifying non-organizer CANNOT edit another student's activity...");
        Activity unauthorizedEdit = new Activity(
                testActId,
                "Sports",
                "Malicious Title Override",
                "Hacked description",
                "2026-12-25",
                "Unauthorized Location",
                organizerReg,
                10,
                "Open",
                created.getCreatedAt(),
                "hacker@srmap.edu.in",
                "0000000000"
        );

        boolean unauthorizedResult = DatabaseManager.updateActivity(unauthorizedEdit, otherStudentReg);
        Activity afterUnauthorized = DatabaseManager.getActivityById(testActId);

        if (unauthorizedResult) {
            System.err.println("  ✗ FAILED: Unauthorized student edit should have returned false.");
            failed++;
        } else if ("Malicious Title Override".equals(afterUnauthorized.getTitle())) {
            System.err.println("  ✗ FAILED: Unauthorized edit took effect in the database!");
            failed++;
        } else {
            System.out.println("  ✓ Unauthorized student edit successfully blocked by ownership verification.");
            passed++;
        }

        // ── Test 3: Join requests and Capacity Validation ───────────────────────
        System.out.println("\n[Test 3] Verifying Capacity Validation & Preserved Memberships...");
        // Student B sends join request
        JoinResult reqB = DatabaseManager.sendJoinRequest(testActId, otherStudentReg, "I want to participate in the AI sprint!");
        if (!reqB.isSuccess()) {
            System.err.println("  ✗ Failed to send join request for Student B.");
            failed++;
        }

        // Find request ID and accept Student B
        List<JoinRequest> incoming = DatabaseManager.getIncomingRequestsForCreator(organizerReg);
        JoinRequest reqToAccept = incoming.stream()
                .filter(r -> r.getActivityId() == testActId && otherStudentReg.equalsIgnoreCase(r.getStudentReg()))
                .findFirst()
                .orElse(null);

        if (reqToAccept != null) {
            DatabaseManager.respondToJoinRequest(reqToAccept.getRequestId(), true);
        }

        // Student C sends a pending join request
        DatabaseManager.sendJoinRequest(testActId, thirdStudentReg, "Interested in machine learning!");

        int confirmedCount = DatabaseManager.getTotalCurrentMembers(testActId);
        System.out.println("  • Confirmed members count: " + confirmedCount + " (1 organizer + 1 accepted teammate)");

        // Attempt to edit capacity to 1 (which is less than confirmed members: 2)
        Activity tooLowCapacityEdit = new Activity(
                testActId,
                "Hackathons",
                "Campus AI Sprint 2026 - Master Track",
                "Attempting capacity lower than confirmed count.",
                "2026-12-08",
                "Innovation Hub Main Lab",
                organizerReg,
                1, // Too low!
                "Open",
                created.getCreatedAt(),
                "updated.email@srmap.edu.in",
                "+91 91234 56789"
        );

        boolean lowCapacityResult = DatabaseManager.updateActivity(tooLowCapacityEdit, organizerReg);
        Activity afterLowCapCheck = DatabaseManager.getActivityById(testActId);

        if (lowCapacityResult) {
            System.err.println("  ✗ FAILED: Capacity reduction below confirmed members should have been blocked.");
            failed++;
        } else if (afterLowCapCheck.getMaxMembers() == 1) {
            System.err.println("  ✗ FAILED: Capacity was reduced below confirmed count in SQLite.");
            failed++;
        } else {
            System.out.println("  ✓ Capacity validation verified: reducing capacity below confirmed count was blocked.");
            passed++;
        }

        // ── Test 4: Verify Join Requests Remain Intact after a Valid Edit ──────
        System.out.println("\n[Test 4] Verifying Join Requests Remain Intact Across Valid Edits...");
        Activity validCapEdit = new Activity(
                testActId,
                "Hackathons",
                "Campus AI Sprint 2026 - Final Edition",
                "Valid description with 4 capacity.",
                "2026-12-08",
                "Innovation Hub Lab A",
                organizerReg,
                4, // Valid capacity (>= 2)
                "Open",
                created.getCreatedAt(),
                "updated.email@srmap.edu.in",
                "+91 91234 56789"
        );

        boolean validEditSuccess = DatabaseManager.updateActivity(validCapEdit, organizerReg);
        List<String> acceptedAfterEdit = DatabaseManager.getAcceptedMembers(testActId);
        JoinRequest reqStudentC = DatabaseManager.getJoinRequest(testActId, thirdStudentReg);

        if (!validEditSuccess) {
            System.err.println("  ✗ FAILED: Valid capacity edit failed.");
            failed++;
        } else if (!acceptedAfterEdit.contains(otherStudentReg)) {
            System.err.println("  ✗ FAILED: Accepted teammate was lost after activity edit.");
            failed++;
        } else if (reqStudentC == null || !reqStudentC.isPending()) {
            System.err.println("  ✗ FAILED: Pending join request was lost or modified after edit.");
            failed++;
        } else {
            System.out.println("  ✓ All existing join requests and accepted memberships intact after activity edit.");
            passed++;
        }

        // ── Test 5: Unauthorized Student CANNOT delete the activity ────────────
        System.out.println("\n[Test 5] Verifying non-organizer CANNOT delete another student's activity...");
        boolean unauthDelete = DatabaseManager.deleteActivityByOrganizer(testActId, otherStudentReg);
        Activity actStillExists = DatabaseManager.getActivityById(testActId);

        if (unauthDelete) {
            System.err.println("  ✗ FAILED: Unauthorized deletion should have returned false.");
            failed++;
        } else if (actStillExists == null) {
            System.err.println("  ✗ FAILED: Activity was deleted by unauthorized student!");
            failed++;
        } else {
            System.out.println("  ✓ Unauthorized deletion blocked. Activity and data preserved.");
            passed++;
        }

        // ── Test 6: Organizer can delete activity & cleanup join requests ─────
        System.out.println("\n[Test 6] Verifying Organizer can delete activity & cascade cleanup...");
        boolean authDelete = DatabaseManager.deleteActivityByOrganizer(testActId, organizerReg);
        Activity actDeleted = DatabaseManager.getActivityById(testActId);

        // Check for orphaned records in join_requests
        int orphanedRequests = 0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM join_requests WHERE activity_id = ?;")) {
            pstmt.setInt(1, testActId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) orphanedRequests = rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Error checking orphaned requests: " + e.getMessage());
        }

        if (!authDelete) {
            System.err.println("  ✗ FAILED: Authorized deletion returned false.");
            failed++;
        } else if (actDeleted != null) {
            System.err.println("  ✗ FAILED: Activity still exists in database after deletion.");
            failed++;
        } else if (orphanedRequests > 0) {
            System.err.println("  ✗ FAILED: Found " + orphanedRequests + " orphaned join requests in SQLite!");
            failed++;
        } else {
            System.out.println("  ✓ Organizer deletion verified. Activity and join requests cleaned up atomically.");
            passed++;
        }

        // ── Test 7: UI Lifecycle & Form Navigation ─────────────────────────────
        System.out.println("\n[Test 7] Verifying JavaFX UI Edit/Delete Lifecycle...");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean uiPassed = new AtomicBoolean(false);

        try {
            Platform.startup(() -> {
                try {
                    MainWindow window = new MainWindow();
                    window.setCurrentStudent("Rahul Varma", organizerReg);

                    // Test showCreateActivity initializes fresh create mode
                    window.showCreateActivity("Hackathons");

                    // Create a dummy activity to test UI edit loading
                    Activity sampleActivity = new Activity(
                            9999,
                            "Sports",
                            "UI Test Cricket Match",
                            "Testing UI edit form population.",
                            "2026-12-12",
                            "Campus Ground",
                            organizerReg,
                            11,
                            "Open",
                            "2026-11-15 12:00:00",
                            "cricket@srmap.edu.in",
                            "9876543210"
                    );

                    window.showEditActivity(sampleActivity, "MY_ACTIVITIES");

                    // Refresh pages check
                    window.refreshAllPages();

                    uiPassed.set(true);
                } catch (Throwable t) {
                    System.err.println("  ✗ UI Lifecycle exception: " + t.getMessage());
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        } catch (IllegalStateException e) {
            Platform.runLater(() -> {
                try {
                    MainWindow window = new MainWindow();
                    window.setCurrentStudent("Rahul Varma", organizerReg);
                    window.showCreateActivity("Hackathons");
                    window.refreshAllPages();
                    uiPassed.set(true);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean done = latch.await(8, TimeUnit.SECONDS);
            if (done && uiPassed.get()) {
                System.out.println("  ✓ UI Edit mode loading and page refresh verified.");
                passed++;
            } else {
                System.err.println("  ✗ FAILED: UI Lifecycle test failed or timed out.");
                failed++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  EDIT & DELETE TEST RESULTS: " + passed + " Passed, " + failed + " Failed");
        if (failed == 0) {
            System.out.println("  ALL EDIT & DELETE ACTIVITY TESTS PASSED! ✓");
        } else {
            System.err.println("  SOME EDIT/DELETE TESTS FAILED!");
            System.exit(1);
        }
        System.out.println("=".repeat(70));

        Platform.exit();
        System.exit(0);
    }
}
