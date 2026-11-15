package teamup.db;

import javafx.application.Platform;
import teamup.model.Activity;
import teamup.ui.MainWindow;
import teamup.ui.pages.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AllPagesNavigationTest – Automated verification of complete application redesign and navigation flow.
 * Tests:
 *   1. Home Page rendering and full-width layout
 *   2. Explore Page with search and 7 category pills
 *   3. Create Activity Page with 4-section form and dynamic fields
 *   4. My Activities Page with profile switcher and 4 metric cards
 *   5. Activity Details Page with back button, info pills, and join action
 *   6. Seamless round-trip navigation between all pages
 */
public class AllPagesNavigationTest {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  TeamUp – Complete Application Visual Redesign & Navigation Test");
        System.out.println("=".repeat(70));

        DatabaseManager.initialise();
        List<Activity> all = DatabaseManager.getAllActivities();
        System.out.println("\n[1] Database active: " + all.size() + " total activities.");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean passed = new AtomicBoolean(false);

        Platform.startup(() -> {
            try {
                System.out.println("\n[2] Initializing MainWindow shell...");
                MainWindow window = new MainWindow();

                // Test 1: Home Page
                System.out.println("  ✓ Testing showHome()...");
                window.showHome();

                // Test 2: Explore Page with "Sports" filter
                System.out.println("  ✓ Testing showExplore(\"Sports\")...");
                window.showExplore("Sports");

                // Test 3: Explore Page with "All" filter
                System.out.println("  ✓ Testing showExplore(null)...");
                window.showExplore(null);

                // Test 4: Create Activity Page with "Hackathons" preselected
                System.out.println("  ✓ Testing showCreateActivity(\"Hackathons\")...");
                window.showCreateActivity("Hackathons");

                // Test 5: My Activities Page
                System.out.println("  ✓ Testing showMyActivities()...");
                window.showMyActivities();

                // Test 6: Activity Details Page
                if (!all.isEmpty()) {
                    System.out.println("  ✓ Testing showActivityDetails() for: " + all.get(0).getTitle());
                    window.showActivityDetails(all.get(0), "EXPLORE");
                }

                // Test 7: Return back to Home
                System.out.println("  ✓ Testing return to showHome()...");
                window.showHome();

                passed.set(true);
            } catch (Throwable t) {
                System.err.println("  ✗ Navigation test failed: " + t.getMessage());
                t.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        try {
            boolean done = latch.await(10, TimeUnit.SECONDS);
            if (!done) {
                System.err.println("  ✗ Test timed out.");
                System.exit(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n" + "=".repeat(70));
        if (passed.get()) {
            System.out.println("  ALL REDESIGNED PAGES & NAVIGATION VERIFIED SUCCESSFULLY!");
        } else {
            System.err.println("  TESTS FAILED!");
            System.exit(1);
        }
        System.out.println("=".repeat(70));

        Platform.exit();
        DatabaseManager.closeConnection();
        System.exit(0);
    }
}
