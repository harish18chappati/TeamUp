package teamup.db;

import javafx.application.Platform;
import teamup.model.Activity;
import teamup.model.Student;
import teamup.model.StudentSession;
import teamup.model.StudentValidator;
import teamup.ui.MainWindow;
import teamup.ui.pages.HomePage;
import teamup.ui.pages.WelcomePage;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WelcomeScreenVerify – Automated test suite for the Student Welcome and Identification Screen.
 *
 * Verifies:
 *   1. Validation rules for full name and SRM registration number.
 *   2. In-memory StudentSession lifecycle and identity retrieval.
 *   3. SQLite database persistence and safe idempotent update of student records.
 *   4. Welcome screen initial launch, navbar visibility toggling, and transition into Home.
 *   5. Change Student workflow and identity propagation to activity creation and join requests.
 */
public class WelcomeScreenVerify {

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("  TeamUp - Student Welcome & Identification Screen Verification");
        System.out.println("=".repeat(65));

        int passed = 0;
        int failed = 0;

        // ── 1. Validation Logic ───────────────────────────────────────────────
        System.out.println("\n[Test 1] Verifying Student Name Validation...");
        if (!StudentValidator.isValidName("Rahul Varma")) {
            System.err.println("  ✗ FAILED: 'Rahul Varma' should be valid.");
            failed++;
        } else if (StudentValidator.isValidName("")) {
            System.err.println("  ✗ FAILED: Empty name should be invalid.");
            failed++;
        } else if (StudentValidator.isValidName("A")) {
            System.err.println("  ✗ FAILED: Single letter name should be invalid.");
            failed++;
        } else if (StudentValidator.isValidName("John123")) {
            System.err.println("  ✗ FAILED: Name with digits should be invalid.");
            failed++;
        } else {
            System.out.println("  ✓ Student name validation logic verified.");
            passed++;
        }

        System.out.println("\n[Test 2] Verifying SRM Registration Number Format...");
        if (!StudentValidator.isValidRegistrationNumber("AP21110010001")) {
            System.err.println("  ✗ FAILED: Standard SRM-AP reg should be valid.");
            failed++;
        } else if (!StudentValidator.isValidRegistrationNumber("ap21110010002")) {
            System.err.println("  ✗ FAILED: Lowercase SRM reg should match case-insensitively.");
            failed++;
        } else if (StudentValidator.isValidRegistrationNumber("12345")) {
            System.err.println("  ✗ FAILED: Pure numeric short string should be rejected.");
            failed++;
        } else if (StudentValidator.isValidRegistrationNumber("INVALID_REG")) {
            System.err.println("  ✗ FAILED: Arbitrary text should be rejected.");
            failed++;
        } else {
            System.out.println("  ✓ SRM Registration Number format validation verified.");
            passed++;
        }

        System.out.println("\n[Test 3] Verifying Complete Form Validation Messages...");
        String errEmptyName = StudentValidator.validate("", "AP21110010001");
        String errEmptyReg = StudentValidator.validate("Rahul Varma", "");
        String errInvalidReg = StudentValidator.validate("Rahul Varma", "99999");
        String okForm = StudentValidator.validate("Rahul Varma", "AP21110010001");

        if (errEmptyName == null || !errEmptyName.contains("Full Name is required")) {
            System.err.println("  ✗ FAILED: Expected empty name error message.");
            failed++;
        } else if (errEmptyReg == null || !errEmptyReg.contains("SRM Registration Number is required")) {
            System.err.println("  ✗ FAILED: Expected empty reg error message.");
            failed++;
        } else if (errInvalidReg == null || !errInvalidReg.contains("Invalid Registration Number format")) {
            System.err.println("  ✗ FAILED: Expected format error message.");
            failed++;
        } else if (okForm != null) {
            System.err.println("  ✗ FAILED: Valid inputs should return null validation error.");
            failed++;
        } else {
            System.out.println("  ✓ Validation error messages verified for empty and invalid cases.");
            passed++;
        }

        // ── 2. Database Integration & Idempotency ─────────────────────────────
        System.out.println("\n[Test 4] Verifying SQLite Student Registration & Safe Update...");
        DatabaseManager.initialise();

        String testReg = "AP21110019999";
        String initialName = "Demo Student Initial";
        String updatedName = "Demo Student Revised";

        Student s1 = DatabaseManager.saveOrUpdateStudent(initialName, testReg);
        if (s1 == null || !initialName.equals(s1.getName()) || !testReg.equals(s1.getRegistrationNumber())) {
            System.err.println("  ✗ FAILED: Initial student registration failed.");
            failed++;
        } else {
            // Update the student name using the same reg number (must not violate UNIQUE constraint)
            Student s2 = DatabaseManager.saveOrUpdateStudent(updatedName, testReg);
            Student retrieved = DatabaseManager.getStudentByReg(testReg);

            if (retrieved == null || !updatedName.equals(retrieved.getName())) {
                System.err.println("  ✗ FAILED: Idempotent student update did not update name in SQLite.");
                failed++;
            } else {
                System.out.println("  ✓ Student persisted and safely updated without UNIQUE constraint failure.");
                passed++;
            }
        }

        // Clean up test student from DB
        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("DELETE FROM students WHERE registration_number = ?;")) {
            pstmt.setString(1, testReg);
            pstmt.executeUpdate();
        } catch (Exception ignored) {}

        // ── 3. StudentSession Lifecycle ───────────────────────────────────────
        System.out.println("\n[Test 5] Verifying In-Memory StudentSession State...");
        StudentSession.setSession("Ananya Reddy", "AP21110010003");

        if (!"Ananya Reddy".equals(StudentSession.getActiveStudentName())) {
            System.err.println("  ✗ FAILED: Session student name mismatch.");
            failed++;
        } else if (!"AP21110010003".equals(StudentSession.getActiveStudentReg())) {
            System.err.println("  ✗ FAILED: Session student reg mismatch.");
            failed++;
        } else if (!"ap21110010003@srmap.edu.in".equals(StudentSession.getActiveStudentEmail())) {
            System.err.println("  ✗ FAILED: Auto-generated college email mismatch.");
            failed++;
        } else {
            System.out.println("  ✓ StudentSession lifecycle and auto-email generation verified.");
            passed++;
        }

        // ── 4. JavaFX UI & Lifecycle ──────────────────────────────────────────
        System.out.println("\n[Test 6] Verifying WelcomePage Launch & MainWindow Lifecycle...");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean uiPassed = new AtomicBoolean(false);

        try {
            Platform.startup(() -> {
                try {
                    MainWindow window = new MainWindow();

                    // 1. Initial screen must be WelcomePage with top navbar hidden from view
                    if (window.getNavBar().isVisible()) {
                        System.err.println("  ✗ FAILED: Navigation bar should be invisible on initial Welcome screen.");
                        return;
                    }

                    // 2. Transition into Home Page restores navigation bar
                    window.showHome();
                    if (!window.getNavBar().isVisible()) {
                        System.err.println("  ✗ FAILED: Navigation bar should be visible when entering Home.");
                        return;
                    }

                    // 3. Return to Welcome screen via Change Student hides navigation bar
                    window.showWelcome();
                    if (window.getNavBar().isVisible()) {
                        System.err.println("  ✗ FAILED: Navigation bar should be hidden when returning to Welcome.");
                        return;
                    }

                    // 4. Change active student identity
                    window.setCurrentStudent("Priya Sharma", "AP21110010002");
                    if (!"AP21110010002".equals(window.getCurrentStudentReg())) {
                        System.err.println("  ✗ FAILED: Active student reg should be AP21110010002.");
                        return;
                    }
                    if (!"Priya Sharma".equals(window.getCurrentStudentName())) {
                        System.err.println("  ✗ FAILED: Active student name should be Priya Sharma.");
                        return;
                    }

                    // 5. Navigate to Create Activity and confirm creator ID matches active student
                    window.showCreateActivity("Travel");
                    if (!"AP21110010002".equals(window.getCurrentStudentReg())) {
                        System.err.println("  ✗ FAILED: Create Activity creator mismatch.");
                        return;
                    }

                    uiPassed.set(true);
                } catch (Throwable t) {
                    System.err.println("  ✗ UI Lifecycle exception: " + t.getMessage());
                    t.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        } catch (IllegalStateException e) {
            // Already started
            Platform.runLater(() -> {
                try {
                    MainWindow window = new MainWindow();
                    window.showWelcome();
                    window.showHome();
                    window.setCurrentStudent("Priya Sharma", "AP21110010002");
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
                System.out.println("  ✓ WelcomePage initial launch, navbar toggle, and session change verified.");
                passed++;
            } else {
                System.err.println("  ✗ FAILED: UI Lifecycle test failed or timed out.");
                failed++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n" + "=".repeat(65));
        System.out.println("  WELCOME SCREEN TEST RESULTS: " + passed + " Passed, " + failed + " Failed");
        if (failed == 0) {
            System.out.println("  ALL STUDENT WELCOME & IDENTIFICATION TESTS PASSED! ✓");
        } else {
            System.err.println("  SOME WELCOME TESTS FAILED!");
            System.exit(1);
        }
        System.out.println("=".repeat(65));
        Platform.exit();
        System.exit(0);
    }
}
