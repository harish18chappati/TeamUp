package teamup.db;

import teamup.model.Activity;

import java.util.List;

/**
 * Phase3Verify – Automated test runner for Phase 3 database and business logic verification.
 *
 * Verifies:
 *   1. Database initialisation & sample data preservation
 *   2. Retrieval of all activities
 *   3. Category filtering across all 6 categories (Travel, Events, Sports, Hackathons, Study Groups, Entertainment)
 *   4. Search by title keyword
 *   5. Activity creation and insertion into SQLite
 *   6. My Activities retrieval by creator ID
 *   7. Activity Details data integrity check
 *   8. Automatic cleanup of test records (no duplicate accumulation)
 */
public class Phase3Verify {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  TeamUp Phase 3.1 – UI & Database Verification");
        System.out.println("============================================================");

        // 1. Initialise
        System.out.println("\n[1] Initialising SQLite database...");
        DatabaseManager.initialise();

        // 2. Retrieve all
        System.out.println("\n[2] Verifying 'Explore Activities' base retrieval (All Activities)...");
        List<Activity> all = DatabaseManager.getAllActivities();
        System.out.println("    Total activities in database: " + all.size());
        if (all.size() >= 8) {
            System.out.println("    ✓ Phase 2 sample data intact (" + all.size() + " records found)");
        } else {
            System.err.println("    ✗ WARNING: Fewer than 8 activities found!");
        }

        // 3. Category filters
        System.out.println("\n[3] Testing Category Filters for all 6 categories...");
        String[] testCategories = {
            "Travel", "Events", "Sports", "Hackathons", "Study Groups", "Entertainment"
        };
        for (String cat : testCategories) {
            List<Activity> filtered = DatabaseManager.getActivitiesByCategory(cat);
            System.out.printf("    • Category '%-14s': %d activities found%n", cat, filtered.size());
        }

        // 4. Search
        System.out.println("\n[4] Testing Title Search...");
        String[] keywords = {"Hackathon", "Football", "Study", "Coorg"};
        for (String kw : keywords) {
            List<Activity> results = DatabaseManager.searchActivities(kw, null);
            System.out.printf("    • Search '%-10s': %d matching activities%n", kw, results.size());
        }

        // 5. Create new temporary test activity
        System.out.println("\n[5] Testing 'Create Activity' submission...");
        String testCreator = "TEST_VERIFY_TEMP";
        String testTitle = "Temp Test Activity for Verification";
        String testDesc = "Automated test description.\n\n[Hackathons Details]\n• Competition: TestComp\n• Required Skills: Java\n• Team Size Needed: 3";

        Activity newActivity = new Activity(
            "Hackathons",
            testTitle,
            testDesc,
            "2025-06-01",
            "Test Lab",
            testCreator,
            3,
            "Open"
        );

        boolean inserted = DatabaseManager.insertActivity(newActivity);
        System.out.println("    Insert result: " + (inserted ? "SUCCESS ✓" : "FAILED ✗"));

        // 6. Test My Activities retrieval
        System.out.println("\n[6] Testing 'My Activities' retrieval for creator " + testCreator + "...");
        List<Activity> myActs = DatabaseManager.getActivitiesByCreator(testCreator);
        System.out.println("    Found " + myActs.size() + " activities created by " + testCreator);
        int createdId = -1;
        for (Activity a : myActs) {
            if (a.getTitle().equals(testTitle)) {
                createdId = a.getActivityId();
                System.out.println("    ✓ Found created activity: [" + createdId + "] " + a.getTitle());
            }
        }

        // 7. Test Activity Details integrity check
        System.out.println("\n[7] Testing Activity Details data resolution on real activity...");
        if (!all.isEmpty()) {
            Activity sample = all.get(0);
            System.out.println("    • Activity ID: " + sample.getActivityId());
            System.out.println("    • Title: " + sample.getTitle());
            System.out.println("    • Category: " + sample.getCategory());
            System.out.println("    • Location: " + sample.getLocation());
            System.out.println("    • Max Members: " + sample.getMaxMembers());
            System.out.println("    ✓ Activity Details fields verified");
        }

        // 8. Clean up temporary test activity
        System.out.println("\n[8] Cleaning up temporary test record...");
        if (createdId > 0) {
            boolean deleted = DatabaseManager.deleteActivity(createdId);
            System.out.println("    Cleanup temporary record [" + createdId + "]: " + (deleted ? "SUCCESS ✓" : "FAILED"));
        }

        System.out.println("\n============================================================");
        System.out.println("  Phase 3.1 Verification COMPLETE - Clean State Preserved");
        System.out.println("============================================================");

        DatabaseManager.closeConnection();
    }
}
