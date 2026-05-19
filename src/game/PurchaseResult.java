package game;

public class PurchaseResult {
    private final boolean success;
    private final String message;

    private PurchaseResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static PurchaseResult success(String message) {
        return new PurchaseResult(true, message);
    }

    public static PurchaseResult failed(String message) {
        return new PurchaseResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
