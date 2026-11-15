package teamup.model;

/**
 * StudentSession – Holds the active student's session information in memory.
 *
 * Used application-wide to associate activities with the current student,
 * populate join requests, check organizer permissions, and display student
 * identity in the navigation/profile area.
 */
public class StudentSession {

    private static String activeStudentName = "Harish Kumar";
    private static String activeStudentReg = "AP21110010001";
    private static String activeStudentEmail = "ap21110010001@srmap.edu.in";
    private static boolean identified = false;

    /**
     * Sets the active student session from name and registration number.
     */
    public static synchronized void setSession(String name, String regNumber) {
        if (regNumber != null && !regNumber.isBlank()) {
            activeStudentReg = StudentValidator.normalizeRegNumber(regNumber);
            activeStudentEmail = activeStudentReg.toLowerCase() + "@srmap.edu.in";
        }
        if (name != null && !name.isBlank()) {
            activeStudentName = StudentValidator.normalizeName(name);
        }
        identified = true;
    }

    /**
     * Sets the active student session from a Student model instance.
     */
    public static synchronized void setStudent(Student student) {
        if (student != null) {
            setSession(student.getName(), student.getRegistrationNumber());
            if (student.getCollegeEmail() != null && !student.getCollegeEmail().isBlank()) {
                activeStudentEmail = student.getCollegeEmail().trim();
            }
        }
    }

    public static synchronized String getActiveStudentName() {
        return activeStudentName;
    }

    public static synchronized String getActiveStudentReg() {
        return activeStudentReg;
    }

    public static synchronized String getActiveStudentEmail() {
        return activeStudentEmail;
    }

    public static synchronized boolean isIdentified() {
        return identified;
    }

    public static synchronized Student toStudent() {
        return new Student(activeStudentName, activeStudentReg, activeStudentEmail);
    }

    /**
     * Resets the session back to default demonstration identity.
     */
    public static synchronized void clear() {
        identified = false;
        activeStudentName = "Harish Kumar";
        activeStudentReg = "AP21110010001";
        activeStudentEmail = "ap21110010001@srmap.edu.in";
    }
}
