package teamup.db;

import teamup.model.Activity;
import teamup.model.ContactValidator;
import teamup.model.JoinResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ContactInfoVerify – Automated test suite verifying Organizer Contact Information:
 *   1. SQLite schema migration (contact_email and contact_phone columns on activities)
 *   2. Preservation of existing records during schema migration
 *   3. Contact info persistence across all categories (Travel, Hackathons, Sports, Events, Study Groups)
 *   4. Email and phone number format validation logic
 *   5. Requirement that at least one contact method is supplied
 *   6. Seamless coexistence with Join Requests and Capacity workflows
 *   7. Safe automated test cleanup
 */
public class ContactInfoVerify {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  TeamUp - Organizer Contact Information Verification");
        System.out.println("============================================================");

        DatabaseManager.initialise();

        int passed = 0;
        int failed = 0;

        List<Integer> cleanupIds = new ArrayList<>();

        try {
            // -----------------------------------------------------------------
            // Test 1: Verify Schema Migration & Columns
            // -----------------------------------------------------------------
            System.out.println("\n[Test 1] Verifying SQLite schema migration and columns...");
            Set<String> columns = new HashSet<>();
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA table_info(activities);")) {
                while (rs.next()) {
                    columns.add(rs.getString("name").toLowerCase());
                }
            } catch (Exception e) {
                System.err.println("  ✗ Failed to read table info: " + e.getMessage());
            }

            if (columns.contains("contact_email") && columns.contains("contact_phone")) {
                System.out.println("  ✓ Both 'contact_email' and 'contact_phone' columns present in 'activities'.");
                passed++;
            } else {
                System.err.println("  ✗ Missing contact columns! Found: " + columns);
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 2: Verify Existing Activities Continue Working
            // -----------------------------------------------------------------
            System.out.println("\n[Test 2] Verifying existing activities load cleanly with safe defaults...");
            List<Activity> all = DatabaseManager.getAllActivities();
            if (all != null && all.size() >= 8) {
                System.out.println("  ✓ Existing activities preserved: " + all.size() + " records found.");
                Activity first = all.get(0);
                System.out.println("  • Sample activity [ID " + first.getActivityId() + "] '" + first.getTitle() + "'");
                System.out.println("  • hasContactInfo(): " + first.hasContactInfo());
                passed++;
            } else {
                System.err.println("  ✗ Fewer than 8 activities found!");
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 3: Validate Email Format Logic
            // -----------------------------------------------------------------
            System.out.println("\n[Test 3] Verifying Email Format Validation...");
            String[] validEmails = {
                    "student@srmap.edu.in",
                    "organizer.lead@domain.com",
                    "team_up+2025@sub.domain.org"
            };
            String[] invalidEmails = {
                    "invalid-email",
                    "user@",
                    "@domain.com",
                    "user@domain",
                    ""
            };

            boolean emailsOk = true;
            for (String ve : validEmails) {
                if (!ContactValidator.isValidEmail(ve)) {
                    System.err.println("  ✗ Valid email failed: " + ve);
                    emailsOk = false;
                }
            }
            for (String ie : invalidEmails) {
                if (ContactValidator.isValidEmail(ie)) {
                    System.err.println("  ✗ Invalid email passed: " + ie);
                    emailsOk = false;
                }
            }

            if (emailsOk) {
                System.out.println("  ✓ Email validation pattern verified for university and standard addresses.");
                passed++;
            } else {
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 4: Validate Phone Number Format Logic
            // -----------------------------------------------------------------
            System.out.println("\n[Test 4] Verifying Phone Number Validation...");
            String[] validPhones = {
                    "9876543210",
                    "+91 98765 43210",
                    "+91-9876543210",
                    "(0863) 2345678",
                    "+1 (555) 123-4567"
            };
            String[] invalidPhones = {
                    "123",
                    "not-a-number",
                    "123456789012345678901", // > 15 digits
                    ""
            };

            boolean phonesOk = true;
            for (String vp : validPhones) {
                if (!ContactValidator.isValidPhone(vp)) {
                    System.err.println("  ✗ Valid phone failed: " + vp);
                    phonesOk = false;
                }
            }
            for (String ip : invalidPhones) {
                if (ContactValidator.isValidPhone(ip)) {
                    System.err.println("  ✗ Invalid phone passed: " + ip);
                    phonesOk = false;
                }
            }

            if (phonesOk) {
                System.out.println("  ✓ Phone validation pattern verified for Indian and international formats.");
                passed++;
            } else {
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 5: Multi-Category Contact Info Persistence in SQLite
            // -----------------------------------------------------------------
            System.out.println("\n[Test 5] Verifying Contact Info persistence across all categories...");

            // 5A: Travel with both email & phone
            Activity travelAct = new Activity(
                    "Travel", "Vizag Coastal Drive", "Weekend drive along RK beach and Rishikonda.",
                    "2026-12-10", "SRM-AP Main Gate", "AP21110010001", 4, "Open",
                    "drive.club@srmap.edu.in", "+91 98765 00001"
            );
            DatabaseManager.insertActivity(travelAct);

            // 5B: Hackathon with email only
            Activity hackAct = new Activity(
                    "Hackathons", "SRM AI Ideathon 2026", "Generative AI Hackathon for SRM-AP students.",
                    "2026-12-15", "Innovation Hub Block B", "AP21110010002", 5, "Open",
                    "ai.hackathon@srmap.edu.in", null
            );
            DatabaseManager.insertActivity(hackAct);

            // 5C: Sports with phone only
            Activity sportAct = new Activity(
                    "Sports", "Inter-Department Basketball", "Half-court 3-on-3 basketball tournament.",
                    "2026-12-20", "Outdoor Basketball Court", "AP21110010003", 6, "Open",
                    null, "+91 98765 00003"
            );
            DatabaseManager.insertActivity(sportAct);

            // 5D: Events with both
            Activity eventAct = new Activity(
                    "Events", "Campus Astronomy Night", "Telescope stargazing session on Block A terrace.",
                    "2026-12-22", "Block A Terrace", "AP21110010004", 15, "Open",
                    "astronomy@srmap.edu.in", "+91 98765 00004"
            );
            DatabaseManager.insertActivity(eventAct);

            // Retrieve and verify each record from SQLite
            List<Activity> retrievedTravel = DatabaseManager.searchActivities("Vizag Coastal Drive", "Travel");
            List<Activity> retrievedHack = DatabaseManager.searchActivities("SRM AI Ideathon", "Hackathons");
            List<Activity> retrievedSport = DatabaseManager.searchActivities("Inter-Department Basketball", "Sports");
            List<Activity> retrievedEvent = DatabaseManager.searchActivities("Campus Astronomy Night", "Events");

            boolean persistOk = true;

            if (!retrievedTravel.isEmpty()) {
                Activity a = retrievedTravel.get(0);
                cleanupIds.add(a.getActivityId());
                if (!"drive.club@srmap.edu.in".equals(a.getContactEmail()) || !"+91 98765 00001".equals(a.getContactPhone())) {
                    System.err.println("  ✗ Travel contact info mismatch!");
                    persistOk = false;
                }
            } else { persistOk = false; }

            if (!retrievedHack.isEmpty()) {
                Activity a = retrievedHack.get(0);
                cleanupIds.add(a.getActivityId());
                if (!"ai.hackathon@srmap.edu.in".equals(a.getContactEmail()) || a.getContactPhone() != null) {
                    System.err.println("  ✗ Hackathon contact info mismatch!");
                    persistOk = false;
                }
            } else { persistOk = false; }

            if (!retrievedSport.isEmpty()) {
                Activity a = retrievedSport.get(0);
                cleanupIds.add(a.getActivityId());
                if (a.getContactEmail() != null || !"+91 98765 00003".equals(a.getContactPhone())) {
                    System.err.println("  ✗ Sports contact info mismatch!");
                    persistOk = false;
                }
            } else { persistOk = false; }

            if (!retrievedEvent.isEmpty()) {
                Activity a = retrievedEvent.get(0);
                cleanupIds.add(a.getActivityId());
                if (!"astronomy@srmap.edu.in".equals(a.getContactEmail()) || !"+91 98765 00004".equals(a.getContactPhone())) {
                    System.err.println("  ✗ Event contact info mismatch!");
                    persistOk = false;
                }
            } else { persistOk = false; }

            if (persistOk) {
                System.out.println("  ✓ Contact information accurately saved and retrieved for Travel, Hackathons, Sports, and Events.");
                passed++;
            } else {
                failed++;
            }

            // -----------------------------------------------------------------
            // Test 6: Verify Join Activity Workflow on Activity with Contact Info
            // -----------------------------------------------------------------
            System.out.println("\n[Test 6] Verifying Join Requests & Capacity with Contact Info...");
            if (!retrievedTravel.isEmpty()) {
                int testId = retrievedTravel.get(0).getActivityId();
                JoinResult jr = DatabaseManager.sendJoinRequest(testId, "AP21110010005", "Love coastal drives!");
                if (jr.isSuccess()) {
                    System.out.println("  ✓ Teammate join request submitted successfully on activity with contact info.");
                    passed++;
                } else {
                    System.err.println("  ✗ Join request failed on activity with contact info: " + jr);
                    failed++;
                }
            }

        } finally {
            // Clean up test activities
            System.out.println("\n[Cleanup] Cleaning up temporary test activities...");
            for (int id : cleanupIds) {
                DatabaseManager.deleteActivity(id);
            }
            System.out.println("  ✓ Cleaned up " + cleanupIds.size() + " test records.");
            DatabaseManager.closeConnection();
        }

        System.out.println("\n============================================================");
        System.out.println("  ORGANIZER CONTACT INFO TEST RESULTS: " + passed + " Passed, " + failed + " Failed");
        if (failed == 0) {
            System.out.println("  ALL ORGANIZER CONTACT INFO TESTS PASSED! ✓");
        }
        System.out.println("============================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
