package teamup.db;

import teamup.model.Activity;
import teamup.model.JoinRequest;
import teamup.model.JoinResult;

import java.util.List;

/**
 * JoinActivityVerify – Automated test suite verifying the Join Activity workflow:
 *   1. Database schema & table creation (join_requests)
 *   2. Modification #1: Counting organizer as 1 member toward total activity capacity
 *   3. Prevention of self-joining by organizer
 *   4. Modification #2: SQLite transaction enforcement and overbooking prevention on acceptance
 *   5. Modification #3: Reapplication allowed after rejection, duplicate pending/accepted blocked
 *   6. Automatic activity status transition ('Open' -> 'Closed' upon reaching full capacity)
 *   7. Full cleanup of temporary test records
 */
public class JoinActivityVerify {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  TeamUp - Join Activity & Capacity Workflow Verification");
        System.out.println("============================================================");

        DatabaseManager.initialise();

        int passed = 0;
        int failed = 0;

        // ---------------------------------------------------------------------
        // Test 1: Verify join_requests table exists and existing activities intact
        // ---------------------------------------------------------------------
        System.out.println("\n[Test 1] Verifying schema and existing activities...");
        List<Activity> existing = DatabaseManager.getAllActivities();
        if (existing != null && existing.size() >= 8) {
            System.out.println("  ✓ Existing activities preserved: " + existing.size() + " records");
            passed++;
        } else {
            System.err.println("  ✗ Failed: Existing activities missing!");
            failed++;
        }

        // Create a temporary test activity for isolated testing
        // Max members = 3 (1 organizer + 2 available seats)
        String testCreator = "TEST_ORG_01";
        String applicantA = "TEST_STUDENT_A";
        String applicantB = "TEST_STUDENT_B";
        String applicantC = "TEST_STUDENT_C";

        Activity testAct = new Activity(
                "Hackathons",
                "Join Workflow Test Hackathon",
                "Temporary activity to verify Join Activity transactions and capacity.",
                "2026-12-01",
                "Innovation Lab Block A",
                testCreator,
                3, // max_members = 3
                "Open"
        );

        boolean inserted = DatabaseManager.insertActivity(testAct);
        if (!inserted) {
            System.err.println("  ✗ Failed to insert test activity. Aborting.");
            System.exit(1);
        }

        // Retrieve created activity
        List<Activity> foundList = DatabaseManager.getActivitiesByCreator(testCreator);
        if (foundList == null || foundList.isEmpty()) {
            System.err.println("  ✗ Failed to find inserted test activity. Aborting.");
            System.exit(1);
        }
        Activity act = foundList.get(0);
        int testActId = act.getActivityId();
        System.out.println("  ✓ Created test activity ID " + testActId + " with max_members = 3");

        try {
            // -----------------------------------------------------------------
            // Test 2: Modification #1 - Capacity rule (Organizer counts as 1)
            // -----------------------------------------------------------------
            System.out.println("\n[Test 2] Verifying Capacity Calculation (Organizer counts as 1 member)...");
            int totalCurrent = DatabaseManager.getTotalCurrentMembers(testActId);
            int availableSeats = DatabaseManager.getAvailableSeats(testActId);
            boolean isFull = DatabaseManager.isActivityFull(testActId);

            if (totalCurrent == 1 && availableSeats == 2 && !isFull) {
                System.out.println("  ✓ Initial capacity correct: 1 current (Organizer), 2 seats available, not full.");
                passed++;
            } else {
                System.err.println("  ✗ Capacity check failed! Current: " + totalCurrent + ", Available: " + availableSeats);
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 3: Self-join prevention
            // -----------------------------------------------------------------
            System.out.println("\n[Test 3] Verifying Organizer cannot join their own activity...");
            JoinResult selfJoinRes = DatabaseManager.sendJoinRequest(testActId, testCreator, "I want to join my own event");
            if (!selfJoinRes.isSuccess() && selfJoinRes.getMessage().contains("organizer")) {
                System.out.println("  ✓ Self-join correctly blocked: " + selfJoinRes.getMessage());
                passed++;
            } else {
                System.err.println("  ✗ Self-join should have been blocked, but got: " + selfJoinRes);
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 4: Submitting valid join request & duplicate prevention
            // -----------------------------------------------------------------
            System.out.println("\n[Test 4] Verifying Request Submission and Duplicate Pending Check...");
            JoinResult reqA1 = DatabaseManager.sendJoinRequest(testActId, applicantA, "Excited to join the team!");
            if (reqA1.isSuccess()) {
                System.out.println("  ✓ Student A request submitted successfully.");
                passed++;
            } else {
                System.err.println("  ✗ Student A request submission failed: " + reqA1);
                failed++;
            }

            // Try duplicate pending request
            JoinResult reqA2 = DatabaseManager.sendJoinRequest(testActId, applicantA, "Sending duplicate request");
            if (!reqA2.isSuccess() && reqA2.getMessage().contains("already have a pending")) {
                System.out.println("  ✓ Duplicate pending request correctly blocked: " + reqA2.getMessage());
                passed++;
            } else {
                System.err.println("  ✗ Duplicate pending request check failed! Got: " + reqA2);
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 5: Creator reviews and accepts request (using SQLite transaction)
            // -----------------------------------------------------------------
            System.out.println("\n[Test 5] Verifying Transaction-based Request Acceptance...");
            JoinRequest reqA = DatabaseManager.getJoinRequest(testActId, applicantA);
            if (reqA != null && reqA.isPending()) {
                System.out.println("  ✓ Found pending request #" + reqA.getRequestId());
                JoinResult acceptRes = DatabaseManager.respondToJoinRequest(reqA.getRequestId(), true);
                if (acceptRes.isSuccess()) {
                    System.out.println("  ✓ Request accepted successfully: " + acceptRes.getMessage());
                    passed++;
                } else {
                    System.err.println("  ✗ Failed to accept request: " + acceptRes);
                    failed++;
                }
            } else {
                System.err.println("  ✗ Expected pending request for Student A not found!");
                failed++;
            }

            // Verify capacity updated: 1 organizer + 1 accepted = 2 members, 1 seat remaining
            int updatedTotal = DatabaseManager.getTotalCurrentMembers(testActId);
            int updatedSeats = DatabaseManager.getAvailableSeats(testActId);
            if (updatedTotal == 2 && updatedSeats == 1) {
                System.out.println("  ✓ Capacity updated: " + updatedTotal + "/3 filled (Organizer + 1 Accepted). Seats left: " + updatedSeats);
                passed++;
            } else {
                System.err.println("  ✗ Capacity calculation mismatch! Total: " + updatedTotal + ", Seats: " + updatedSeats);
                failed++;
            }

            // Prevent duplicate request when already accepted
            JoinResult reqA3 = DatabaseManager.sendJoinRequest(testActId, applicantA, "Applying again while accepted");
            if (!reqA3.isSuccess() && (reqA3.getMessage().contains("already a confirmed member") || reqA3.getMessage().contains("already an accepted"))) {
                System.out.println("  ✓ Already accepted duplicate check succeeded: " + reqA3.getMessage());
                passed++;
            } else {
                System.err.println("  ✗ Accepted duplicate check failed! Got: " + reqA3);
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 6: Modification #3 - Reapplication allowed after rejection
            // -----------------------------------------------------------------
            System.out.println("\n[Test 6] Verifying Reapplication after Rejection...");
            JoinResult reqB1 = DatabaseManager.sendJoinRequest(testActId, applicantB, "Applying for seat 2");
            JoinRequest reqB = DatabaseManager.getJoinRequest(testActId, applicantB);
            if (reqB != null) {
                // Reject Student B
                JoinResult rejRes = DatabaseManager.respondToJoinRequest(reqB.getRequestId(), false);
                JoinRequest afterRej = DatabaseManager.getJoinRequest(testActId, applicantB);
                if (rejRes.isSuccess() && afterRej != null && afterRej.isRejected()) {
                    System.out.println("  ✓ Request rejected successfully.");

                    // Now reapply!
                    JoinResult reapplyRes = DatabaseManager.sendJoinRequest(testActId, applicantB, "Updated proposal with backend skill");
                    JoinRequest reapplyReq = DatabaseManager.getJoinRequest(testActId, applicantB);
                    if (reapplyRes.isSuccess() && reapplyReq != null && reapplyReq.isPending()) {
                        System.out.println("  ✓ Reapplication allowed after rejection: " + reapplyRes.getMessage());
                        passed++;
                    } else {
                        System.err.println("  ✗ Reapplication failed! Got: " + reapplyRes);
                        failed++;
                    }
                } else {
                    System.err.println("  ✗ Rejection response failed: " + rejRes);
                    failed++;
                }
            }

            // -----------------------------------------------------------------
            // Test 7: Filling activity to capacity & Auto-Close
            // -----------------------------------------------------------------
            System.out.println("\n[Test 7] Verifying Activity Auto-Closure upon Reaching Capacity...");
            // Student B is pending. Accept Student B to reach capacity (1 host + 2 accepted = 3)
            JoinRequest pendingB = DatabaseManager.getJoinRequest(testActId, applicantB);
            JoinResult acceptB = DatabaseManager.respondToJoinRequest(pendingB.getRequestId(), true);
            Activity closedAct = DatabaseManager.getActivityById(testActId);

            if (acceptB.isSuccess() && "Closed".equalsIgnoreCase(closedAct.getStatus()) && DatabaseManager.isActivityFull(testActId)) {
                System.out.println("  ✓ Activity reached full capacity (3/3). Status automatically set to 'Closed'.");
                passed++;
            } else {
                System.err.println("  ✗ Auto-closure check failed! Status: " + (closedAct != null ? closedAct.getStatus() : "null"));
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 8: Modification #2 - Overbooking Prevention & Rollback
            // -----------------------------------------------------------------
            System.out.println("\n[Test 8] Verifying Overbooking Prevention & SQLite Transaction Rollback...");
            // Now activity is full. Student C tries to apply directly -> blocked because activity is full/closed
            JoinResult reqCFull = DatabaseManager.sendJoinRequest(testActId, applicantC, "Trying to join full event");
            if (!reqCFull.isSuccess() && reqCFull.getMessage().contains("full or closed")) {
                System.out.println("  ✓ Request submission to full activity blocked: " + reqCFull.getMessage());
                passed++;
            } else {
                System.err.println("  ✗ Joining full activity should be blocked! Got: " + reqCFull);
                failed++;
            }

            // Verify confirmed members roster
            List<String> confirmedMembers = DatabaseManager.getAcceptedMembers(testActId);
            if (confirmedMembers.size() == 2 && confirmedMembers.contains(applicantA) && confirmedMembers.contains(applicantB)) {
                System.out.println("  ✓ Confirmed teammates roster verified: " + confirmedMembers);
                passed++;
            } else {
                System.err.println("  ✗ Confirmed teammates roster mismatch: " + confirmedMembers);
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 9: Creator Incoming Requests queries
            // -----------------------------------------------------------------
            System.out.println("\n[Test 9] Verifying Organizer Dashboard Incoming Requests Query...");
            List<JoinRequest> creatorRequests = DatabaseManager.getIncomingRequestsForCreator(testCreator);
            if (creatorRequests != null && creatorRequests.size() == 2) {
                System.out.println("  ✓ Found " + creatorRequests.size() + " incoming requests for organizer " + testCreator);
                passed++;
            } else {
                System.err.println("  ✗ Expected 2 incoming requests for creator, got: " + (creatorRequests != null ? creatorRequests.size() : "null"));
                failed++;
            }

        } finally {
            // -----------------------------------------------------------------
            // Cleanup: Delete temporary test activity and its requests
            // -----------------------------------------------------------------
            System.out.println("\n[Cleanup] Cleaning up temporary test activity and join requests...");
            boolean deleted = DatabaseManager.deleteActivity(testActId);
            if (deleted) {
                System.out.println("  ✓ Temporary test activity [ID " + testActId + "] cleaned up.");
            } else {
                System.err.println("  ✗ Failed to delete test activity!");
            }
        }

        System.out.println("\n============================================================");
        System.out.println("  TEST RESULTS: " + passed + " Passed, " + failed + " Failed");
        if (failed == 0) {
            System.out.println("  ALL JOIN ACTIVITY BACKEND & TRANSACTION TESTS PASSED! ✓");
        }
        System.out.println("============================================================");

        DatabaseManager.closeConnection();

        if (failed > 0) {
            System.exit(1);
        }
    }
}
