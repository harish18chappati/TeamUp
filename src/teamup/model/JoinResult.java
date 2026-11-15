package teamup.model;

/**
 * JoinResult – Encapsulates the outcome of a join request submission or review.
 */
public class JoinResult {

    private final boolean success;
    private final String message;

    public JoinResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static JoinResult ok(String message) {
        return new JoinResult(true, message);
    }

    public static JoinResult fail(String message) {
        return new JoinResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return (success ? "SUCCESS: " : "FAILED: ") + message;
    }
}
