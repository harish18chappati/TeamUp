package teamup.model;

/**
 * Student – Represents a student user of the TeamUp platform.
 *
 * Fields match the students table in the SQLite database:
 *   student_id          INTEGER PRIMARY KEY AUTOINCREMENT
 *   name                TEXT NOT NULL
 *   registration_number TEXT UNIQUE NOT NULL
 *   college_email       TEXT UNIQUE NOT NULL
 */
public class Student {

    private int    studentId;           // Primary key (auto-assigned by DB)
    private String name;                // Full name, e.g. "Harish Kumar"
    private String registrationNumber;  // SRM reg number, e.g. "AP21110010001"
    private String collegeEmail;        // College email, e.g. "ap21110010001@srmap.edu.in"

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** No-argument constructor (useful for testing). */
    public Student() {}

    /**
     * Constructor used when creating a new student before saving to DB.
     * student_id is not needed here because the database assigns it automatically.
     */
    public Student(String name, String registrationNumber, String collegeEmail) {
        this.name               = name;
        this.registrationNumber = registrationNumber;
        this.collegeEmail       = collegeEmail;
    }

    /**
     * Constructor used when reading a student row from the database.
     * All four fields (including the DB-assigned ID) are provided.
     */
    public Student(int studentId, String name, String registrationNumber, String collegeEmail) {
        this.studentId          = studentId;
        this.name               = name;
        this.registrationNumber = registrationNumber;
        this.collegeEmail       = collegeEmail;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public int    getStudentId()                        { return studentId; }
    public void   setStudentId(int studentId)           { this.studentId = studentId; }

    public String getName()                             { return name; }
    public void   setName(String name)                  { this.name = name; }

    public String getRegistrationNumber()               { return registrationNumber; }
    public void   setRegistrationNumber(String rn)      { this.registrationNumber = rn; }

    public String getCollegeEmail()                     { return collegeEmail; }
    public void   setCollegeEmail(String collegeEmail)  { this.collegeEmail = collegeEmail; }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Student{id=" + studentId
                + ", name='" + name + "'"
                + ", regNo='" + registrationNumber + "'"
                + ", email='" + collegeEmail + "'}";
    }
}
