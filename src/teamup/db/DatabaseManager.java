package teamup.db;

import teamup.model.Activity;
import teamup.model.JoinRequest;
import teamup.model.JoinResult;
import teamup.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DatabaseManager – Manages all SQLite database operations for TeamUp.
 *
 * Responsibilities:
 *   1. Open and maintain a single shared Connection to teamup.db.
 *   2. Create the students and activities tables if they do not exist.
 *   3. Insert sample data when the database is first created (empty).
 *   4. Provide reusable INSERT and SELECT methods used by the UI.
 *
 * All database operations use PreparedStatement to prevent SQL injection.
 * Database file location: data/teamup.db (inside the project folder)
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:data/teamup.db";
    private static Connection connection = null;

    /** Returns (or creates) the shared SQLite connection. */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("[DB] Connected to SQLite: data/teamup.db");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "SQLite JDBC driver not found. Make sure sqlite-jdbc-*.jar is in lib/.", e);
            }
        }
        return connection;
    }

    /** Closes the shared connection. Call on application exit. */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }

    // ── Table creation ────────────────────────────────────────────────────────

    public static void createTables() {
        String createStudents = """
                CREATE TABLE IF NOT EXISTS students (
                    student_id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    name                TEXT    NOT NULL,
                    registration_number TEXT    NOT NULL UNIQUE,
                    college_email       TEXT    NOT NULL UNIQUE
                );
                """;

        String createActivities = """
                CREATE TABLE IF NOT EXISTS activities (
                    activity_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    category      TEXT    NOT NULL,
                    title         TEXT    NOT NULL,
                    description   TEXT,
                    activity_date TEXT,
                    location      TEXT,
                    created_by    TEXT,
                    max_members   INTEGER DEFAULT 5,
                    status        TEXT    DEFAULT 'Open',
                    created_at    TEXT    DEFAULT (datetime('now','localtime')),
                    contact_email TEXT,
                    contact_phone TEXT
                );
                """;

        String createJoinRequests = """
                CREATE TABLE IF NOT EXISTS join_requests (
                    request_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    activity_id  INTEGER NOT NULL,
                    student_reg  TEXT    NOT NULL,
                    status       TEXT    NOT NULL DEFAULT 'Pending',
                    message      TEXT,
                    requested_at TEXT    DEFAULT (datetime('now','localtime')),
                    responded_at TEXT,
                    FOREIGN KEY (activity_id) REFERENCES activities(activity_id) ON DELETE CASCADE
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createStudents);
            System.out.println("[DB] Table 'students' ready.");
            stmt.execute(createActivities);
            System.out.println("[DB] Table 'activities' ready.");
            stmt.execute(createJoinRequests);
            System.out.println("[DB] Table 'join_requests' ready.");
        } catch (SQLException e) {
            System.err.println("[DB] Error creating tables: " + e.getMessage());
        }
    }

    // ── Safe Database Migration ──────────────────────────────────────────────

    /**
     * Safely migrates existing SQLite tables without losing or resetting any data.
     * Ensures 'contact_email' and 'contact_phone' columns exist on the 'activities' table.
     */
    public static void migrateSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            Set<String> existingColumns = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(activities);")) {
                while (rs.next()) {
                    existingColumns.add(rs.getString("name").toLowerCase());
                }
            }

            if (!existingColumns.contains("contact_email")) {
                stmt.execute("ALTER TABLE activities ADD COLUMN contact_email TEXT;");
                System.out.println("[DB] Migration: Added column 'contact_email' to table 'activities'.");
            }

            if (!existingColumns.contains("contact_phone")) {
                stmt.execute("ALTER TABLE activities ADD COLUMN contact_phone TEXT;");
                System.out.println("[DB] Migration: Added column 'contact_phone' to table 'activities'.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error running schema migration: " + e.getMessage());
        }
    }

    // ── Empty check ───────────────────────────────────────────────────────────

    public static boolean isActivitiesTableEmpty() {
        String sql = "SELECT COUNT(*) FROM activities;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error checking activities count: " + e.getMessage());
        }
        return false;
    }

    // ── Insert student ────────────────────────────────────────────────────────

    public static boolean insertStudent(Student student) {
        String sql = "INSERT INTO students (name, registration_number, college_email) VALUES (?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getRegistrationNumber());
            pstmt.setString(3, student.getCollegeEmail());
            int rows = pstmt.executeUpdate();
            System.out.println("[DB] Inserted student: " + student.getName());
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error inserting student: " + e.getMessage());
            return false;
        }
    }

    /**
     * Looks up an existing student by registration number (case-insensitive).
     * @return Student if found, or null if not registered yet.
     */
    public static Student getStudentByReg(String regNumber) {
        if (regNumber == null || regNumber.isBlank()) return null;
        String sql = "SELECT student_id, name, registration_number, college_email FROM students WHERE UPPER(registration_number) = UPPER(?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, regNumber.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("registration_number"),
                        rs.getString("college_email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error finding student by reg: " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves or updates a student identification record in the database.
     * Safely handles previously registered students by updating their display name if needed,
     * without causing UNIQUE constraint violations or resetting existing data.
     */
    public static Student saveOrUpdateStudent(String name, String regNumber) {
        if (name == null || regNumber == null || regNumber.isBlank()) return null;
        String cleanReg = regNumber.trim().toUpperCase();
        String cleanName = name.trim();
        String cleanEmail = cleanReg.toLowerCase() + "@srmap.edu.in";

        Student existing = getStudentByReg(cleanReg);
        if (existing != null) {
            // Student already exists in DB - update name if changed
            if (!cleanName.equalsIgnoreCase(existing.getName())) {
                String updateSql = "UPDATE students SET name = ? WHERE student_id = ?;";
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, cleanName);
                    pstmt.setInt(2, existing.getStudentId());
                    pstmt.executeUpdate();
                    existing.setName(cleanName);
                    System.out.println("[DB] Updated student profile: " + cleanName + " (" + cleanReg + ")");
                } catch (SQLException e) {
                    System.err.println("[DB] Error updating student name: " + e.getMessage());
                }
            }
            return existing;
        } else {
            // New student: insert into students table
            String insertSql = "INSERT INTO students (name, registration_number, college_email) VALUES (?, ?, ?);";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, cleanName);
                pstmt.setString(2, cleanReg);
                pstmt.setString(3, cleanEmail);
                pstmt.executeUpdate();
                int id = 0;
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) id = generatedKeys.getInt(1);
                }
                System.out.println("[DB] Registered new student: " + cleanName + " (" + cleanReg + ")");
                return new Student(id, cleanName, cleanReg, cleanEmail);
            } catch (SQLException e) {
                System.err.println("[DB] Error registering student: " + e.getMessage());
                return new Student(0, cleanName, cleanReg, cleanEmail);
            }
        }
    }

    // ── Insert activity ───────────────────────────────────────────────────────

    public static boolean insertActivity(Activity activity) {
        String sql = "INSERT INTO activities " +
                     "(category, title, description, activity_date, location, " +
                     " created_by, max_members, status, contact_email, contact_phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activity.getCategory());
            pstmt.setString(2, activity.getTitle());
            pstmt.setString(3, activity.getDescription());
            pstmt.setString(4, activity.getActivityDate());
            pstmt.setString(5, activity.getLocation());
            pstmt.setString(6, activity.getCreatedBy());
            pstmt.setInt   (7, activity.getMaxMembers());
            pstmt.setString(8, activity.getStatus());
            pstmt.setString(9, activity.getContactEmail());
            pstmt.setString(10, activity.getContactPhone());
            int rows = pstmt.executeUpdate();
            System.out.println("[DB] Inserted activity: " + activity.getTitle());
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error inserting activity: " + e.getMessage());
            return false;
        }
    }

    // ── Delete activity (for test cleanup and lifecycle management) ───────────

    public static boolean deleteActivity(int activityId) {
        String deleteRequestsSql = "DELETE FROM join_requests WHERE activity_id = ?;";
        String sql = "DELETE FROM activities WHERE activity_id = ?;";
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtReq = conn.prepareStatement(deleteRequestsSql)) {
                pstmtReq.setInt(1, activityId);
                pstmtReq.executeUpdate();
            }
            int rows = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, activityId);
                rows = pstmt.executeUpdate();
            }

            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[DB] Error deleting activity: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Deletes an activity and all its associated join requests inside an atomic SQLite transaction.
     * Enforces organizer authorization: only the creator of the activity can delete it.
     *
     * @param activityId   the ID of the activity to delete
     * @param organizerReg the registration number of the current active student attempting deletion
     * @return true if deletion succeeded, false if unauthorized or database error
     */
    public static boolean deleteActivityByOrganizer(int activityId, String organizerReg) {
        if (organizerReg == null || organizerReg.isBlank()) {
            System.err.println("[DB] Delete rejected: organizer registration number is missing.");
            return false;
        }

        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // START SQLITE TRANSACTION

            // 1. Verify organizer ownership inside the transaction
            String checkSql = "SELECT created_by, title FROM activities WHERE activity_id = ?;";
            String creator = null;
            String title = null;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, activityId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        creator = rs.getString("created_by");
                        title = rs.getString("title");
                    }
                }
            }

            if (creator == null) {
                conn.rollback();
                System.err.println("[DB] Delete rejected: activity #" + activityId + " does not exist.");
                return false;
            }

            if (!creator.trim().equalsIgnoreCase(organizerReg.trim())) {
                conn.rollback();
                System.err.println("[DB] Delete UNAUTHORIZED: student " + organizerReg + " is not the creator of activity #" + activityId + " (owned by " + creator + ").");
                return false;
            }

            // 2. Delete all associated join requests (prevents orphaned records)
            String delRequestsSql = "DELETE FROM join_requests WHERE activity_id = ?;";
            int deletedRequests = 0;
            try (PreparedStatement delReqStmt = conn.prepareStatement(delRequestsSql)) {
                delReqStmt.setInt(1, activityId);
                deletedRequests = delReqStmt.executeUpdate();
            }

            // 3. Delete the activity record
            String delActivitySql = "DELETE FROM activities WHERE activity_id = ? AND LOWER(created_by) = LOWER(?);";
            int rowsDeleted = 0;
            try (PreparedStatement delActStmt = conn.prepareStatement(delActivitySql)) {
                delActStmt.setInt(1, activityId);
                delActStmt.setString(2, organizerReg.trim());
                rowsDeleted = delActStmt.executeUpdate();
            }

            conn.commit(); // COMMIT TRANSACTION
            System.out.println("[DB] Successfully deleted activity #" + activityId + " (\"" + title + "\") and " + deletedRequests + " associated join requests by organizer " + organizerReg);
            return rowsDeleted > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[DB] Error deleting activity by organizer: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Updates an existing activity in the SQLite database.
     * Enforces organizer authorization and capacity constraints:
     *   - Only the creator can update the activity.
     *   - Maximum members cannot be lower than the current number of confirmed members (organizer + accepted teammates).
     *   - Preserves all existing join requests and accepted memberships intact.
     *
     * @param activity     the Activity containing updated field values
     * @param organizerReg the registration number of the current active student attempting edit
     * @return true if update succeeded, false if unauthorized, capacity invalid, or error
     */
    public static boolean updateActivity(Activity activity, String organizerReg) {
        if (activity == null || activity.getActivityId() <= 0) {
            System.err.println("[DB] Update rejected: invalid activity object.");
            return false;
        }
        if (organizerReg == null || organizerReg.isBlank()) {
            System.err.println("[DB] Update rejected: organizer registration number is missing.");
            return false;
        }

        int activityId = activity.getActivityId();
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // START SQLITE TRANSACTION

            // 1. Verify organizer ownership inside the transaction
            String checkSql = "SELECT created_by, status FROM activities WHERE activity_id = ?;";
            String creator = null;
            String currentStatus = null;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, activityId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        creator = rs.getString("created_by");
                        currentStatus = rs.getString("status");
                    }
                }
            }

            if (creator == null) {
                conn.rollback();
                System.err.println("[DB] Update rejected: activity #" + activityId + " does not exist.");
                return false;
            }

            if (!creator.trim().equalsIgnoreCase(organizerReg.trim())) {
                conn.rollback();
                System.err.println("[DB] Update UNAUTHORIZED: student " + organizerReg + " is not creator of activity #" + activityId);
                return false;
            }

            // 2. Validate capacity against confirmed members (1 organizer + accepted requests)
            int acceptedCount = 0;
            String countSql = "SELECT COUNT(*) FROM join_requests WHERE activity_id = ? AND status = 'Accepted';";
            try (PreparedStatement cntStmt = conn.prepareStatement(countSql)) {
                cntStmt.setInt(1, activityId);
                try (ResultSet rs = cntStmt.executeQuery()) {
                    if (rs.next()) acceptedCount = rs.getInt(1);
                }
            }

            int confirmedMembers = 1 + acceptedCount;
            if (activity.getMaxMembers() < confirmedMembers) {
                conn.rollback();
                System.err.println("[DB] Update rejected: new capacity (" + activity.getMaxMembers() +
                        ") is lower than confirmed members (" + confirmedMembers + ").");
                return false;
            }

            // 3. Status adjustment based on capacity:
            // If capacity is reached, status is 'Closed'.
            // If capacity is expanded and status was 'Closed' due to fullness, re-open to 'Open'.
            String newStatus = activity.getStatus();
            if (confirmedMembers >= activity.getMaxMembers()) {
                newStatus = "Closed";
            } else if ("Closed".equalsIgnoreCase(currentStatus) && "Open".equalsIgnoreCase(activity.getStatus())) {
                newStatus = "Open";
            }

            // 4. Execute atomic update
            String updateSql = "UPDATE activities SET " +
                    "category = ?, " +
                    "title = ?, " +
                    "description = ?, " +
                    "activity_date = ?, " +
                    "location = ?, " +
                    "max_members = ?, " +
                    "status = ?, " +
                    "contact_email = ?, " +
                    "contact_phone = ? " +
                    "WHERE activity_id = ? AND LOWER(created_by) = LOWER(?);";

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, activity.getCategory());
                pstmt.setString(2, activity.getTitle());
                pstmt.setString(3, activity.getDescription());
                pstmt.setString(4, activity.getActivityDate());
                pstmt.setString(5, activity.getLocation());
                pstmt.setInt   (6, activity.getMaxMembers());
                pstmt.setString(7, newStatus);
                pstmt.setString(8, activity.getContactEmail());
                pstmt.setString(9, activity.getContactPhone());
                pstmt.setInt   (10, activityId);
                pstmt.setString(11, organizerReg.trim());

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    conn.commit(); // COMMIT TRANSACTION
                    System.out.println("[DB] Updated activity #" + activityId + " (\"" + activity.getTitle() + "\") successfully.");
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[DB] Error updating activity: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }
        }
    }

    // ── Retrieve single activity by ID ───────────────────────────────────────

    public static Activity getActivityById(int activityId) {
        String sql = "SELECT activity_id, category, title, description, " +
                     "activity_date, location, created_by, max_members, status, created_at, " +
                     "contact_email, contact_phone " +
                     "FROM activities WHERE activity_id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving activity by id: " + e.getMessage());
        }
        return null;
    }

    // ── Update activity status ('Open' or 'Closed') ───────────────────────────

    public static boolean updateActivityStatus(int activityId, String status) {
        String sql = "UPDATE activities SET status = ? WHERE activity_id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, activityId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error updating activity status: " + e.getMessage());
            return false;
        }
    }

    // ── Capacity & Membership Calculation (Organizer counts as 1 member) ─────

    /**
     * Counts accepted teammate join requests for an activity.
     */
    public static int getAcceptedMembersCount(int activityId) {
        String sql = "SELECT COUNT(*) FROM join_requests WHERE activity_id = ? AND status = 'Accepted';";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error counting accepted members: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts total current members in an activity.
     * Modification #1: Counts the organizer as one member when calculating total activity capacity.
     */
    public static int getTotalCurrentMembers(int activityId) {
        return 1 + getAcceptedMembersCount(activityId);
    }

    /**
     * Calculates remaining available seats (max_members - (1 + acceptedCount)).
     */
    public static int getAvailableSeats(int activityId) {
        Activity act = getActivityById(activityId);
        if (act == null) return 0;
        int totalCurrent = getTotalCurrentMembers(activityId);
        int available = act.getMaxMembers() - totalCurrent;
        return Math.max(0, available);
    }

    /**
     * Checks if an activity has reached full capacity.
     */
    public static boolean isActivityFull(int activityId) {
        Activity act = getActivityById(activityId);
        if (act == null) return true;
        return getTotalCurrentMembers(activityId) >= act.getMaxMembers();
    }

    /**
     * Returns the list of registration numbers of accepted members.
     */
    public static List<String> getAcceptedMembers(int activityId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT student_reg FROM join_requests WHERE activity_id = ? AND status = 'Accepted' ORDER BY request_id ASC;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(rs.getString("student_reg"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving accepted members: " + e.getMessage());
        }
        return list;
    }

    // ── Join Requests Management ──────────────────────────────────────────────

    /**
     * Fetches the latest join request record for a student on an activity.
     */
    public static JoinRequest getJoinRequest(int activityId, String studentReg) {
        if (studentReg == null || studentReg.isBlank()) return null;
        String sql = "SELECT request_id, activity_id, student_reg, status, message, requested_at, responded_at " +
                     "FROM join_requests WHERE activity_id = ? AND LOWER(student_reg) = LOWER(?) ORDER BY request_id DESC LIMIT 1;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            pstmt.setString(2, studentReg.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRequestRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error getting join request: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets current status: "Pending", "Accepted", "Rejected", or null if no request.
     */
    public static String getJoinRequestStatus(int activityId, String studentReg) {
        JoinRequest req = getJoinRequest(activityId, studentReg);
        return req != null ? req.getStatus() : null;
    }

    /**
     * Submits a join request from a student.
     * Modification #3: Allows reapplication after rejection, but prevents duplicate pending or accepted requests.
     */
    public static JoinResult sendJoinRequest(int activityId, String studentReg, String message) {
        if (studentReg == null || studentReg.isBlank()) {
            return JoinResult.fail("Student registration number is required.");
        }
        String trimmedReg = studentReg.trim();
        Activity activity = getActivityById(activityId);
        if (activity == null) {
            return JoinResult.fail("Activity not found.");
        }

        // Prevent self-join (organizer is already member #1)
        if (activity.getCreatedBy() != null && activity.getCreatedBy().equalsIgnoreCase(trimmedReg)) {
            return JoinResult.fail("You are the organizer of this activity.");
        }

        // Check capacity and status
        if ("Closed".equalsIgnoreCase(activity.getStatus()) || isActivityFull(activityId)) {
            return JoinResult.fail("This activity is full or closed for new members.");
        }

        // Check existing request
        JoinRequest existing = getJoinRequest(activityId, trimmedReg);
        if (existing != null) {
            if (existing.isAccepted()) {
                return JoinResult.fail("You are already a confirmed member of this activity.");
            }
            if (existing.isPending()) {
                return JoinResult.fail("You already have a pending join request for this activity.");
            }
            if (existing.isRejected()) {
                // Modification #3: Allow reapplication after rejection by resetting to Pending
                String updateSql = "UPDATE join_requests SET status = 'Pending', message = ?, " +
                                   "requested_at = (datetime('now','localtime')), responded_at = NULL " +
                                   "WHERE request_id = ?;";
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, message != null ? message.trim() : "");
                    pstmt.setInt(2, existing.getRequestId());
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        return JoinResult.ok("Reapplication submitted successfully! Awaiting organizer review.");
                    }
                } catch (SQLException e) {
                    System.err.println("[DB] Error updating reapplication: " + e.getMessage());
                    return JoinResult.fail("Database error: " + e.getMessage());
                }
            }
        }

        // Insert fresh join request
        String insertSql = "INSERT INTO join_requests (activity_id, student_reg, status, message) VALUES (?, ?, 'Pending', ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setInt(1, activityId);
            pstmt.setString(2, trimmedReg);
            pstmt.setString(3, message != null ? message.trim() : "");
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                return JoinResult.ok("Join request sent successfully! Awaiting organizer review.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error inserting join request: " + e.getMessage());
            return JoinResult.fail("Database error: " + e.getMessage());
        }

        return JoinResult.fail("Unable to submit join request.");
    }

    /**
     * Responds to a join request (Accept or Reject).
     * Modification #2: Uses SQLite transactions when accepting requests and updating activity status to prevent overbooking.
     */
    public static JoinResult respondToJoinRequest(int requestId, boolean accept) {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // START SQLITE TRANSACTION

            // 1. Fetch request details
            int activityId = -1;
            String studentReg = null;
            String currentStatus = null;
            String reqSql = "SELECT activity_id, student_reg, status FROM join_requests WHERE request_id = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(reqSql)) {
                pstmt.setInt(1, requestId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        activityId = rs.getInt("activity_id");
                        studentReg = rs.getString("student_reg");
                        currentStatus = rs.getString("status");
                    }
                }
            }

            if (activityId == -1) {
                conn.rollback();
                return JoinResult.fail("Join request #" + requestId + " was not found.");
            }

            // 2. Handling REJECTION
            if (!accept) {
                String rejectSql = "UPDATE join_requests SET status = 'Rejected', responded_at = (datetime('now','localtime')) WHERE request_id = ?;";
                try (PreparedStatement pstmt = conn.prepareStatement(rejectSql)) {
                    pstmt.setInt(1, requestId);
                    pstmt.executeUpdate();
                }

                // If previously accepted member is now rejected, re-check capacity and re-open if appropriate
                if ("Accepted".equalsIgnoreCase(currentStatus)) {
                    int maxMembers = 0;
                    try (PreparedStatement actStmt = conn.prepareStatement("SELECT max_members FROM activities WHERE activity_id = ?;")) {
                        actStmt.setInt(1, activityId);
                        try (ResultSet rs = actStmt.executeQuery()) {
                            if (rs.next()) maxMembers = rs.getInt("max_members");
                        }
                    }
                    int acceptedCount = 0;
                    try (PreparedStatement cntStmt = conn.prepareStatement("SELECT COUNT(*) FROM join_requests WHERE activity_id = ? AND status = 'Accepted';")) {
                        cntStmt.setInt(1, activityId);
                        try (ResultSet rs = cntStmt.executeQuery()) {
                            if (rs.next()) acceptedCount = rs.getInt(1);
                        }
                    }
                    if (1 + acceptedCount < maxMembers) {
                        try (PreparedStatement openStmt = conn.prepareStatement("UPDATE activities SET status = 'Open' WHERE activity_id = ?;")) {
                            openStmt.setInt(1, activityId);
                            openStmt.executeUpdate();
                        }
                    }
                }

                conn.commit(); // COMMIT TRANSACTION
                return JoinResult.ok("Request rejected for student " + studentReg + ".");
            }

            // 3. Handling ACCEPTANCE
            // Fetch activity capacity inside the transaction
            int maxMembers = 0;
            String actStatus = null;
            String actSql = "SELECT max_members, status FROM activities WHERE activity_id = ?;";
            try (PreparedStatement actStmt = conn.prepareStatement(actSql)) {
                actStmt.setInt(1, activityId);
                try (ResultSet rs = actStmt.executeQuery()) {
                    if (rs.next()) {
                        maxMembers = rs.getInt("max_members");
                        actStatus = rs.getString("status");
                    }
                }
            }

            // Count currently accepted members inside the transaction
            int acceptedCount = 0;
            String countSql = "SELECT COUNT(*) FROM join_requests WHERE activity_id = ? AND status = 'Accepted';";
            try (PreparedStatement cntStmt = conn.prepareStatement(countSql)) {
                cntStmt.setInt(1, activityId);
                try (ResultSet rs = cntStmt.executeQuery()) {
                    if (rs.next()) acceptedCount = rs.getInt(1);
                }
            }

            // Modification #1: Organizer counts as 1 member.
            // Check if capacity is already reached
            if (1 + acceptedCount >= maxMembers) {
                conn.rollback(); // CANCEL & ROLLBACK TRANSACTION
                return JoinResult.fail("Cannot accept request: Activity capacity is already full (" +
                        (1 + acceptedCount) + "/" + maxMembers + " seats occupied).");
            }

            // Mark request as Accepted
            String acceptSql = "UPDATE join_requests SET status = 'Accepted', responded_at = (datetime('now','localtime')) WHERE request_id = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(acceptSql)) {
                pstmt.setInt(1, requestId);
                pstmt.executeUpdate();
            }

            // Check if newly accepted member fills the activity completely
            int newTotalOccupied = 1 + acceptedCount + 1;
            if (newTotalOccupied >= maxMembers) {
                String closeSql = "UPDATE activities SET status = 'Closed' WHERE activity_id = ?;";
                try (PreparedStatement closeStmt = conn.prepareStatement(closeSql)) {
                    closeStmt.setInt(1, activityId);
                    closeStmt.executeUpdate();
                }
                System.out.println("[DB] Activity #" + activityId + " filled to capacity (" +
                        newTotalOccupied + "/" + maxMembers + "). Status set to 'Closed'.");
            }

            conn.commit(); // COMMIT TRANSACTION
            return JoinResult.ok("Request accepted! " + studentReg + " is now a confirmed teammate.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    System.err.println("[DB] Rollback failed: " + rbEx.getMessage());
                }
            }
            System.err.println("[DB] Error during respondToJoinRequest transaction: " + e.getMessage());
            return JoinResult.fail("Database transaction error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Retrieves all join requests for an activity, sorted with Pending first, then by requested_at DESC.
     */
    public static List<JoinRequest> getJoinRequestsForActivity(int activityId) {
        List<JoinRequest> list = new ArrayList<>();
        String sql = "SELECT request_id, activity_id, student_reg, status, message, requested_at, responded_at " +
                     "FROM join_requests WHERE activity_id = ? " +
                     "ORDER BY CASE status WHEN 'Pending' THEN 1 WHEN 'Accepted' THEN 2 ELSE 3 END, request_id DESC;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRequestRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving activity requests: " + e.getMessage());
        }
        return list;
    }

    /**
     * Gets count of pending requests for a specific activity.
     */
    public static int getPendingRequestsCountForActivity(int activityId) {
        String sql = "SELECT COUNT(*) FROM join_requests WHERE activity_id = ? AND status = 'Pending';";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error counting pending requests: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves all incoming join requests across all activities created by a specific student.
     */
    public static List<JoinRequest> getIncomingRequestsForCreator(String creatorReg) {
        List<JoinRequest> list = new ArrayList<>();
        if (creatorReg == null || creatorReg.isBlank()) return list;

        String sql = "SELECT r.request_id, r.activity_id, r.student_reg, r.status, r.message, " +
                     "r.requested_at, r.responded_at, a.title AS activity_title " +
                     "FROM join_requests r " +
                     "JOIN activities a ON r.activity_id = a.activity_id " +
                     "WHERE LOWER(a.created_by) = LOWER(?) " +
                     "ORDER BY CASE r.status WHEN 'Pending' THEN 1 WHEN 'Accepted' THEN 2 ELSE 3 END, r.request_id DESC;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, creatorReg.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    JoinRequest req = mapRequestRow(rs);
                    req.setActivityTitle(rs.getString("activity_title"));
                    list.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving creator incoming requests: " + e.getMessage());
        }
        return list;
    }

    /**
     * Gets count of pending requests across all activities created by a student.
     */
    public static int getPendingRequestsCountForCreator(String creatorReg) {
        if (creatorReg == null || creatorReg.isBlank()) return 0;
        String sql = "SELECT COUNT(*) FROM join_requests r " +
                     "JOIN activities a ON r.activity_id = a.activity_id " +
                     "WHERE LOWER(a.created_by) = LOWER(?) AND r.status = 'Pending';";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, creatorReg.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error counting pending requests for creator: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves all join requests sent by a student across all activities.
     */
    public static List<JoinRequest> getRequestsByStudent(String studentReg) {
        List<JoinRequest> list = new ArrayList<>();
        if (studentReg == null || studentReg.isBlank()) return list;

        String sql = "SELECT r.request_id, r.activity_id, r.student_reg, r.status, r.message, " +
                     "r.requested_at, r.responded_at, a.title AS activity_title " +
                     "FROM join_requests r " +
                     "JOIN activities a ON r.activity_id = a.activity_id " +
                     "WHERE LOWER(r.student_reg) = LOWER(?) " +
                     "ORDER BY r.request_id DESC;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentReg.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    JoinRequest req = mapRequestRow(rs);
                    req.setActivityTitle(rs.getString("activity_title"));
                    list.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving student's outgoing requests: " + e.getMessage());
        }
        return list;
    }

    /**
     * Deletes a specific join request (useful for testing and undo).
     */
    public static boolean deleteJoinRequest(int requestId) {
        String sql = "DELETE FROM join_requests WHERE request_id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error deleting join request: " + e.getMessage());
            return false;
        }
    }

    private static JoinRequest mapRequestRow(ResultSet rs) throws SQLException {
        return new JoinRequest(
            rs.getInt("request_id"),
            rs.getInt("activity_id"),
            rs.getString("student_reg"),
            rs.getString("status"),
            rs.getString("message"),
            rs.getString("requested_at"),
            rs.getString("responded_at")
        );
    }

    // ── Retrieve all activities ───────────────────────────────────────────────

    public static List<Activity> getAllActivities() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT activity_id, category, title, description, " +
                     "activity_date, location, created_by, max_members, status, created_at, " +
                     "contact_email, contact_phone " +
                     "FROM activities ORDER BY activity_id DESC;";
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) activities.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving activities: " + e.getMessage());
        }
        return activities;
    }

    // ── Retrieve by category (supports singular/plural) ───────────────────────

    public static List<Activity> getActivitiesByCategory(String category) {
        List<Activity> activities = new ArrayList<>();
        if (category == null || category.isBlank() || category.equalsIgnoreCase("All") || category.equalsIgnoreCase("All Activities")) {
            return getAllActivities();
        }

        String pattern = getCategorySearchPattern(category);
        String sql = "SELECT activity_id, category, title, description, " +
                     "activity_date, location, created_by, max_members, status, created_at, " +
                     "contact_email, contact_phone " +
                     "FROM activities WHERE LOWER(category) LIKE LOWER(?) ORDER BY activity_id DESC;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) activities.add(mapRow(rs));
            rs.close();
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving by category: " + e.getMessage());
        }
        return activities;
    }

    // ── Retrieve by creator (for My Activities page) ─────────────────────────

    public static List<Activity> getActivitiesByCreator(String createdBy) {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT activity_id, category, title, description, " +
                     "activity_date, location, created_by, max_members, status, created_at, " +
                     "contact_email, contact_phone " +
                     "FROM activities WHERE LOWER(created_by) = LOWER(?) ORDER BY activity_id DESC;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, createdBy.trim());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) activities.add(mapRow(rs));
            rs.close();
        } catch (SQLException e) {
            System.err.println("[DB] Error retrieving by creator: " + e.getMessage());
        }
        return activities;
    }

    // ── Search by title keyword with optional category filter ────────────────

    public static List<Activity> searchActivities(String keyword, String category) {
        List<Activity> activities = new ArrayList<>();
        boolean hasCategory = (category != null && !category.isBlank() &&
                               !category.equalsIgnoreCase("All") &&
                               !category.equalsIgnoreCase("All Activities"));
        String sql;
        if (hasCategory) {
            sql = "SELECT activity_id, category, title, description, " +
                  "activity_date, location, created_by, max_members, status, created_at, " +
                  "contact_email, contact_phone " +
                  "FROM activities WHERE LOWER(title) LIKE LOWER(?) " +
                  "AND LOWER(category) LIKE LOWER(?) ORDER BY activity_id DESC;";
        } else {
            sql = "SELECT activity_id, category, title, description, " +
                  "activity_date, location, created_by, max_members, status, created_at, " +
                  "contact_email, contact_phone " +
                  "FROM activities WHERE LOWER(title) LIKE LOWER(?) ORDER BY activity_id DESC;";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword.trim() + "%");
            if (hasCategory) {
                pstmt.setString(2, getCategorySearchPattern(category));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) activities.add(mapRow(rs));
            rs.close();
        } catch (SQLException e) {
            System.err.println("[DB] Error searching activities: " + e.getMessage());
        }
        return activities;
    }

    /**
     * Helper to match both singular and plural forms (e.g. "Hackathon" and "Hackathons").
     */
    private static String getCategorySearchPattern(String category) {
        String trimmed = category.trim().toLowerCase();
        if (trimmed.startsWith("hackathon")) return "%hackathon%";
        if (trimmed.startsWith("study")) return "%study%";
        if (trimmed.startsWith("sport")) return "%sport%";
        if (trimmed.startsWith("travel")) return "%travel%";
        if (trimmed.startsWith("event")) return "%event%";
        if (trimmed.startsWith("entertainment")) return "%entertainment%";
        return "%" + trimmed + "%";
    }

    // ── Helper: map a ResultSet row to an Activity object ────────────────────

    private static Activity mapRow(ResultSet rs) throws SQLException {
        Activity act = new Activity(
            rs.getInt   ("activity_id"),
            rs.getString("category"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("activity_date"),
            rs.getString("location"),
            rs.getString("created_by"),
            rs.getInt   ("max_members"),
            rs.getString("status"),
            rs.getString("created_at")
        );
        try {
            act.setContactEmail(rs.getString("contact_email"));
        } catch (SQLException ignored) {}
        try {
            act.setContactPhone(rs.getString("contact_phone"));
        } catch (SQLException ignored) {}
        return act;
    }

    // ── Sample data ───────────────────────────────────────────────────────────

    public static void insertSampleData() {
        System.out.println("[DB] Inserting sample activities...");

        insertActivity(new Activity("Travel", "Coorg Weekend Getaway",
            "A relaxing weekend trip to Coorg (Kodagu) – coffee plantations, " +
            "waterfalls, and misty hills. Open to all SRM-AP students. " +
            "Shared cab from campus on Friday evening.",
            "2025-02-14", "Departs from SRM-AP Main Gate", "AP21110010001", 6, "Open"));

        insertActivity(new Activity("Travel", "Araku Valley Day Trip",
            "One-day trip to Araku Valley via the scenic Vizag mountain railway. " +
            "Tribal museum, coffee gardens, and Borra Caves on the itinerary.",
            "2025-03-08", "Visakhapatnam Railway Station", "AP21110010002", 10, "Open"));

        insertActivity(new Activity("Sports", "5-a-Side Football Tournament",
            "Inter-hostel 5-a-side football tournament. Register your team of 5 " +
            "(+ 1 substitute). Knockout rounds over three weekends. " +
            "Trophy and medals for top 3 teams.",
            "2025-02-01", "SRM-AP Sports Ground", "AP21110010003", 6, "Open"));

        insertActivity(new Activity("Sports", "Badminton Doubles League",
            "Mixed and open doubles badminton league. Round-robin format " +
            "followed by semifinals and finals. All skill levels welcome.",
            "2025-02-20", "Indoor Sports Complex, Court 2", "AP21110010004", 2, "Open"));

        insertActivity(new Activity("Hackathon", "Smart India Hackathon 2025 - Internal Round",
            "SRM-AP internal selection round for Smart India Hackathon 2025. " +
            "Build solutions for real government problem statements in 24 hours. " +
            "Top teams represent SRM-AP at the national level.",
            "2025-03-15", "Innovation & Entrepreneurship Hub, Block A", "AP21110010005", 6, "Open"));

        insertActivity(new Activity("Hackathon", "HackForAP - State Level Hackathon",
            "Andhra Pradesh state-level hackathon focused on AgriTech, HealthTech, " +
            "and EdTech. 36-hour event with mentoring sessions and prize pool of Rs.2 Lakhs.",
            "2025-04-05", "Amaravati Convention Centre", "AP21110010006", 4, "Open"));

        insertActivity(new Activity("Study Group", "Data Structures & Algorithms Study Circle",
            "Weekly DSA practice sessions preparing for placement interviews. " +
            "Problems from LeetCode, Codeforces, and GFG. " +
            "One senior mentor + peer discussions every Saturday morning.",
            "2025-01-25", "Library Reading Hall, Room 3", "AP21110010007", 8, "Open"));

        insertActivity(new Activity("Study Group", "Machine Learning Reading Group",
            "Bi-weekly reading group going through Andrew Ng ML Specialization " +
            "and hands-on Kaggle notebooks. Beginners and intermediate learners welcome.",
            "2025-02-05", "CS Department Seminar Hall", "AP21110010008", 12, "Open"));

        System.out.println("[DB] Sample activities inserted successfully.");
    }

    // ── Initialise ────────────────────────────────────────────────────────────

    public static void initialise() {
        System.out.println("[DB] Initialising TeamUp database...");
        try {
            getConnection();
            createTables();
            migrateSchema();
            if (isActivitiesTableEmpty()) {
                insertSampleData();
            } else {
                System.out.println("[DB] Sample data already present - skipping seed.");
            }
            System.out.println("[DB] Database ready.");
        } catch (SQLException e) {
            System.err.println("[DB] FATAL - could not initialise database: " + e.getMessage());
        }
    }
}
