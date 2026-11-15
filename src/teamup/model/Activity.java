package teamup.model;

/**
 * Activity – Represents a campus activity or event posted on TeamUp.
 *
 * Fields match the activities table in the SQLite database:
 *   activity_id   INTEGER PRIMARY KEY AUTOINCREMENT
 *   category      TEXT NOT NULL        (e.g. "Travel", "Sports", "Hackathon", "Study Group")
 *   title         TEXT NOT NULL        (e.g. "Smart India Hackathon 2025")
 *   description   TEXT                 (full description of the activity)
 *   activity_date TEXT                 (e.g. "2025-03-15")
 *   location      TEXT                 (e.g. "Main Auditorium")
 *   created_by    TEXT                 (registration number of the posting student)
 *   max_members   INTEGER              (maximum team size)
 *   status        TEXT DEFAULT 'Open'  (e.g. "Open" or "Closed")
 *   created_at    TEXT                 (timestamp when the activity was posted)
 *   contact_email TEXT                 (organizer email address)
 *   contact_phone TEXT                 (organizer phone number)
 */
public class Activity {

    private int    activityId;   // Primary key (auto-assigned by DB)
    private String category;     // Activity category
    private String title;        // Short activity name
    private String description;  // Full description
    private String activityDate; // Date of the activity (YYYY-MM-DD)
    private String location;     // Where it takes place
    private String createdBy;    // Registration number of the student who posted it
    private int    maxMembers;   // Maximum number of members / team size
    private String status;       // "Open" or "Closed"
    private String createdAt;    // Timestamp when the record was created
    private String contactEmail; // Organizer contact email
    private String contactPhone; // Organizer contact phone number

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** No-argument constructor (useful for testing). */
    public Activity() {}

    /**
     * Legacy constructor used when inserting a new activity without explicit contact info.
     * Preserves backward compatibility with earlier code and tests.
     */
    public Activity(String category, String title, String description,
                    String activityDate, String location, String createdBy,
                    int maxMembers, String status) {
        this(category, title, description, activityDate, location, createdBy, maxMembers, status, null, null);
    }

    /**
     * Complete constructor used when inserting a new activity with organizer contact info.
     */
    public Activity(String category, String title, String description,
                    String activityDate, String location, String createdBy,
                    int maxMembers, String status, String contactEmail, String contactPhone) {
        this.category     = category;
        this.title        = title;
        this.description  = description;
        this.activityDate = activityDate;
        this.location     = location;
        this.createdBy    = createdBy;
        this.maxMembers   = maxMembers;
        this.status       = status;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    /**
     * Legacy constructor used when reading an activity row from the database without contact columns.
     */
    public Activity(int activityId, String category, String title, String description,
                    String activityDate, String location, String createdBy,
                    int maxMembers, String status, String createdAt) {
        this(activityId, category, title, description, activityDate, location, createdBy, maxMembers, status, createdAt, null, null);
    }

    /**
     * Full constructor used when reading an activity row from the database with contact info.
     */
    public Activity(int activityId, String category, String title, String description,
                    String activityDate, String location, String createdBy,
                    int maxMembers, String status, String createdAt,
                    String contactEmail, String contactPhone) {
        this.activityId   = activityId;
        this.category     = category;
        this.title        = title;
        this.description  = description;
        this.activityDate = activityDate;
        this.location     = location;
        this.createdBy    = createdBy;
        this.maxMembers   = maxMembers;
        this.status       = status;
        this.createdAt    = createdAt;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public int    getActivityId()                       { return activityId; }
    public void   setActivityId(int activityId)         { this.activityId = activityId; }

    public String getCategory()                         { return category; }
    public void   setCategory(String category)          { this.category = category; }

    public String getTitle()                            { return title; }
    public void   setTitle(String title)                { this.title = title; }

    public String getDescription()                      { return description; }
    public void   setDescription(String description)    { this.description = description; }

    public String getActivityDate()                     { return activityDate; }
    public void   setActivityDate(String activityDate)  { this.activityDate = activityDate; }

    public String getLocation()                         { return location; }
    public void   setLocation(String location)          { this.location = location; }

    public String getCreatedBy()                        { return createdBy; }
    public void   setCreatedBy(String createdBy)        { this.createdBy = createdBy; }

    public int    getMaxMembers()                       { return maxMembers; }
    public void   setMaxMembers(int maxMembers)         { this.maxMembers = maxMembers; }

    public String getStatus()                           { return status; }
    public void   setStatus(String status)              { this.status = status; }

    public String getCreatedAt()                        { return createdAt; }
    public void   setCreatedAt(String createdAt)        { this.createdAt = createdAt; }

    public String getContactEmail()                     { return contactEmail; }
    public void   setContactEmail(String contactEmail)   { this.contactEmail = contactEmail; }

    public String getContactPhone()                     { return contactPhone; }
    public void   setContactPhone(String contactPhone)   { this.contactPhone = contactPhone; }

    public boolean hasContactEmail() {
        return contactEmail != null && !contactEmail.trim().isEmpty();
    }

    public boolean hasContactPhone() {
        return contactPhone != null && !contactPhone.trim().isEmpty();
    }

    public boolean hasContactInfo() {
        return hasContactEmail() || hasContactPhone();
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Activity{id=" + activityId
                + ", category='" + category + "'"
                + ", title='" + title + "'"
                + ", date='" + activityDate + "'"
                + ", status='" + status + "'"
                + ", email='" + contactEmail + "'"
                + ", phone='" + contactPhone + "'}";
    }
}
