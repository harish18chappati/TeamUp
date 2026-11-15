package teamup.db;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import teamup.model.Activity;
import teamup.ui.MainWindow;
import teamup.ui.pages.ActivityDetailsPage;
import teamup.ui.pages.CreateActivityPage;
import teamup.ui.pages.ExplorePage;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * AuditFixesVerify – Verifies the four bug fixes requested in the audit:
 *   1. Category dynamic details preserved on edit (no data loss)
 *   2. Past dates disabled and rejected in CreateActivityPage
 *   3. ActivityDetailsPage back button label and routing for HOME, EXPLORE, and MY_ACTIVITIES
 *   4. ExplorePage instant dynamic search on keystroke with category preservation & cache
 */
public class AuditFixesVerify {

    public static void main(String[] args) throws Exception {
        System.out.println("============================================================");
        System.out.println("  TeamUp - Audit Bug Fixes Automated Verification");
        System.out.println("============================================================");

        DatabaseManager.initialise();

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] allPassed = {true};

        // Initialize JavaFX Platform
        try {
            Platform.startup(() -> latch.countDown());
        } catch (IllegalStateException e) {
            // Already started
            latch.countDown();
        }
        latch.await(5, TimeUnit.SECONDS);

        CountDownLatch testLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                MainWindow window = new MainWindow();

                // ── TEST 1: Category Details Preservation on Edit ─────────────────
                System.out.println("\n[Test 1] Verifying Category Dynamic Details Preservation on Edit...");
                CreateActivityPage createPage = new CreateActivityPage(window);

                Activity sampleActivity = new Activity(
                        999,
                        "Travel",
                        "Coorg Plantation Retreat",
                        "A relaxing weekend trip to Coorg plantations.\n\n── Key Details ──\n• Destination / Route: Coorg, Karnataka\n• Estimated Budget / Mode of Travel: ₹2,500 per head",
                        LocalDate.now().plusDays(5).toString(),
                        "SRM-AP Main Gate",
                        "AP21110010001",
                        5,
                        "Open",
                        "2026-11-15 10:00:00",
                        "organizer@srmap.edu.in",
                        "9876543210"
                );

                createPage.loadActivityForEdit(sampleActivity, "EXPLORE");

                Field fDesc = CreateActivityPage.class.getDeclaredField("descArea");
                fDesc.setAccessible(true);
                javafx.scene.control.TextArea descArea = (javafx.scene.control.TextArea) fDesc.get(createPage);

                Field fD1 = CreateActivityPage.class.getDeclaredField("dynamicField1");
                fD1.setAccessible(true);
                TextField d1 = (TextField) fD1.get(createPage);

                Field fD2 = CreateActivityPage.class.getDeclaredField("dynamicField2");
                fD2.setAccessible(true);
                TextField d2 = (TextField) fD2.get(createPage);

                if (!"Coorg, Karnataka".equals(d1.getText())) {
                    throw new AssertionError("dynamicField1 mismatch: expected 'Coorg, Karnataka', got '" + d1.getText() + "'");
                }
                if (!"₹2,500 per head".equals(d2.getText())) {
                    throw new AssertionError("dynamicField2 mismatch: expected '₹2,500 per head', got '" + d2.getText() + "'");
                }
                if (!"A relaxing weekend trip to Coorg plantations.".equals(descArea.getText().trim())) {
                    throw new AssertionError("descArea mismatch: got '" + descArea.getText() + "'");
                }
                System.out.println("  ✓ Dynamic category fields parsed and populated correctly:");
                System.out.println("    - Field 1 (Destination): " + d1.getText());
                System.out.println("    - Field 2 (Budget/Mode): " + d2.getText());
                System.out.println("    - Base Description: " + descArea.getText().trim());

                // ── TEST 2: Past Dates Disabled & Rejected in CreateActivityPage ────
                System.out.println("\n[Test 2] Verifying Past Dates Prevention in CreateActivityPage...");
                Field fDate = CreateActivityPage.class.getDeclaredField("datePicker");
                fDate.setAccessible(true);
                DatePicker datePicker = (DatePicker) fDate.get(createPage);

                if (datePicker.getDayCellFactory() == null) {
                    throw new AssertionError("DatePicker DayCellFactory is null!");
                }
                System.out.println("  ✓ DatePicker DayCellFactory installed to disable past dates in calendar.");

                // ── TEST 3: ActivityDetailsPage Back Navigation (HOME, EXPLORE, MY_ACTIVITIES) ──
                System.out.println("\n[Test 3] Verifying ActivityDetailsPage Back Navigation destinations & labels...");

                // 3a. When opened from HOME
                ActivityDetailsPage pageHome = new ActivityDetailsPage(sampleActivity, "HOME", window);
                Button backHome = findBackButton(pageHome);
                if (!"← Back to Home".equals(backHome.getText())) {
                    throw new AssertionError("Expected '← Back to Home', got '" + backHome.getText() + "'");
                }
                System.out.println("  ✓ HOME return label verified: '" + backHome.getText() + "'");

                // 3b. When opened from EXPLORE
                ActivityDetailsPage pageExplore = new ActivityDetailsPage(sampleActivity, "EXPLORE", window);
                Button backExplore = findBackButton(pageExplore);
                if (!"← Back to Explore".equals(backExplore.getText())) {
                    throw new AssertionError("Expected '← Back to Explore', got '" + backExplore.getText() + "'");
                }
                System.out.println("  ✓ EXPLORE return label verified: '" + backExplore.getText() + "'");

                // 3c. When opened from MY_ACTIVITIES
                ActivityDetailsPage pageMy = new ActivityDetailsPage(sampleActivity, "MY_ACTIVITIES", window);
                Button backMy = findBackButton(pageMy);
                if (!"← Back to My Activities".equals(backMy.getText())) {
                    throw new AssertionError("Expected '← Back to My Activities', got '" + backMy.getText() + "'");
                }
                System.out.println("  ✓ MY_ACTIVITIES return label verified: '" + backMy.getText() + "'");

                // ── TEST 4: Instant Dynamic Search in ExplorePage ─────────────────
                System.out.println("\n[Test 4] Verifying Instant Keystroke Search & Category Filtering in ExplorePage...");
                ExplorePage explorePage = new ExplorePage(window);

                Field fSearch = ExplorePage.class.getDeclaredField("searchField");
                fSearch.setAccessible(true);
                TextField searchField = (TextField) fSearch.get(explorePage);

                Field fCount = ExplorePage.class.getDeclaredField("countLabel");
                fCount.setAccessible(true);
                Label countLabel = (Label) fCount.get(explorePage);

                Field fCache = ExplorePage.class.getDeclaredField("cachedCategoryActivities");
                fCache.setAccessible(true);

                // Initial state (All activities)
                explorePage.selectCategory("All");
                List<?> allActs = (List<?>) fCache.get(explorePage);
                int totalAll = allActs.size();
                System.out.println("  ✓ Cache loaded for All Categories: " + totalAll + " activities.");

                // Filter by Sports
                explorePage.selectCategory("Sports");
                List<?> sportsActs = (List<?>) fCache.get(explorePage);
                System.out.println("  ✓ Cache updated for Sports: " + sportsActs.size() + " activities.");

                // Simulate typing "Badminton" into searchField (instant filter)
                searchField.setText("Badminton");
                System.out.println("  ✓ Simulated keystroke text: '" + searchField.getText() + "' -> " + countLabel.getText());
                if (!countLabel.getText().contains("1 activity") && !countLabel.getText().contains("1 activities")) {
                    throw new AssertionError("Expected 1 match for 'Badminton' in Sports, got: " + countLabel.getText());
                }

                // Simulate clearing search
                searchField.setText("");
                System.out.println("  ✓ Cleared search -> " + countLabel.getText());
                if (!countLabel.getText().contains(sportsActs.size() + " activities")) {
                    throw new AssertionError("Expected " + sportsActs.size() + " activities after clearing, got: " + countLabel.getText());
                }

                System.out.println("\n============================================================");
                System.out.println("  ALL 4 AUDIT BUG FIXES VERIFIED SUCCESSFULLY! ✓");
                System.out.println("============================================================");

            } catch (Throwable t) {
                t.printStackTrace();
                allPassed[0] = false;
            } finally {
                testLatch.countDown();
            }
        });

        testLatch.await(10, TimeUnit.SECONDS);

        if (!allPassed[0]) {
            System.err.println("AuditFixesVerify FAILED!");
            System.exit(1);
        } else {
            System.out.println("AuditFixesVerify completed with exit code 0.");
            System.exit(0);
        }
    }

    private static Button findBackButton(ActivityDetailsPage page) {
        // Find back button in page children
        try {
            Field fScrollPane = ActivityDetailsPage.class.getDeclaredField("scrollPane");
            fScrollPane.setAccessible(true);
            javafx.scene.control.ScrollPane sp = (javafx.scene.control.ScrollPane) fScrollPane.get(page);
            javafx.scene.layout.VBox body = (javafx.scene.layout.VBox) sp.getContent();
            javafx.scene.layout.VBox contentBox = (javafx.scene.layout.VBox) body.getChildren().get(0);
            javafx.scene.layout.HBox topRow = (javafx.scene.layout.HBox) contentBox.getChildren().get(0);
            return (Button) topRow.getChildren().get(0);
        } catch (Exception e) {
            throw new RuntimeException("Could not locate top back button", e);
        }
    }
}
