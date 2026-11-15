package teamup.db;

import teamup.model.Activity;
import teamup.model.Student;

import java.util.List;

/**
 * DatabaseVerify – Standalone test class for Phase 2 verification.
 *
 * This class:
 *   1. Initialises the database (creates file + tables + sample data).
 *   2. Retrieves all activities and prints them to the console.
 *   3. Retrieves activities by each category and prints counts.
 *   4. Inserts a test student and confirms the insert.
 *
 * Run with:
 *   java -cp "out;lib/sqlite-jdbc-3.47.0.0.jar" teamup.db.DatabaseVerify
 */
public class DatabaseVerify {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  TeamUp Phase 2 – Database Verification");
        System.out.println("=".repeat(60));

        // ── Step 1: Initialise ───────────────────────────────────────────────
        System.out.println("\n[1] Initialising database...");
        DatabaseManager.initialise();

        // ── Step 2: Retrieve all activities ─────────────────────────────────
        System.out.println("\n[2] Retrieving all activities...");
        List<Activity> all = DatabaseManager.getAllActivities();
        System.out.println("    Total activities found: " + all.size());
        for (Activity a : all) {
            System.out.printf("    [%d] %-15s | %-40s | %s%n",
                a.getActivityId(), a.getCategory(), a.getTitle(), a.getStatus());
        }

        // ── Step 3: Retrieve by category ─────────────────────────────────────
        System.out.println("\n[3] Retrieving activities by category...");
        String[] categories = {"Travel", "Sports", "Hackathon", "Study Group"};
        for (String cat : categories) {
            List<Activity> catList = DatabaseManager.getActivitiesByCategory(cat);
            System.out.printf("    %-15s : %d activities%n", cat, catList.size());
        }

        // ── Step 4: Insert a test student ────────────────────────────────────
        System.out.println("\n[4] Inserting a test student...");
        Student testStudent = new Student(
            "Harish Kumar",
            "AP21110010099",
            "ap21110010099@srmap.edu.in"
        );
        boolean inserted = DatabaseManager.insertStudent(testStudent);
        System.out.println("    Insert result: " + (inserted ? "SUCCESS" : "FAILED (may already exist)"));

        // ── Done ─────────────────────────────────────────────────────────────
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  Phase 2 Verification COMPLETE");
        System.out.println("  Database: data/teamup.db");
        System.out.println("  Tables  : students, activities");
        System.out.println("=".repeat(60));

        DatabaseManager.closeConnection();
    }
}
