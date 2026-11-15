package teamup.model;

/**
 * JoinRequest – Represents a teammate / member application for a campus activity.
 *
 * Fields match the join_requests table in SQLite:
 *   request_id    INTEGER PRIMARY KEY AUTOINCREMENT
 *   activity_id   INTEGER NOT NULL
 *   student_reg   TEXT NOT NULL
 *   status        TEXT NOT NULL DEFAULT 'Pending' ('Pending', 'Accepted', 'Rejected')
 *   message       TEXT
 *   requested_at  TEXT DEFAULT (datetime('now', 'localtime'))
 *   responded_at  TEXT
 */
public class JoinRequest {

    private int requestId;
    private int activityId;
    private String studentReg;
    private String status;      // "Pending", "Accepted", "Rejected"
    private String message;
    private String requestedAt;
    private String respondedAt;

    // Supplementary field for display convenience in feeds and notifications
    private String activityTitle;

    public JoinRequest() {
    }

    public JoinRequest(int activityId, String studentReg, String message) {
        this.activityId = activityId;
        this.studentReg = studentReg;
        this.message = message;
        this.status = "Pending";
    }

    public JoinRequest(int requestId, int activityId, String studentReg, String status,
                       String message, String requestedAt, String respondedAt) {
        this.requestId = requestId;
        this.activityId = activityId;
        this.studentReg = studentReg;
        this.status = status;
        this.message = message;
        this.requestedAt = requestedAt;
        this.respondedAt = respondedAt;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getStudentReg() {
        return studentReg;
    }

    public void setStudentReg(String studentReg) {
        this.studentReg = studentReg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(String respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public boolean isAccepted() {
        return "Accepted".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "Rejected".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "JoinRequest{" +
                "requestId=" + requestId +
                ", activityId=" + activityId +
                ", studentReg='" + studentReg + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", requestedAt='" + requestedAt + '\'' +
                ", respondedAt='" + respondedAt + '\'' +
                '}';
    }
}
