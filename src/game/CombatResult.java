package game;

public class CombatResult {
    private final boolean hit;
    private final boolean critical;
    private final int amount;
    private final String message;

    public CombatResult(boolean hit, boolean critical, int amount, String message) {
        this.hit = hit;
        this.critical = critical;
        this.amount = amount;
        this.message = message;
    }

    public boolean isHit() {
        return hit;
    }

    public boolean isCritical() {
        return critical;
    }

    public int getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}
