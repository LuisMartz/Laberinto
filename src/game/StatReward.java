package game;

public class StatReward {
    private final String label;
    private final StatType statType;
    private final int amount;
    private final int hpBonus;
    private final int mpBonus;

    public StatReward(String label, StatType statType, int amount, int hpBonus, int mpBonus) {
        this.label = label;
        this.statType = statType;
        this.amount = amount;
        this.hpBonus = hpBonus;
        this.mpBonus = mpBonus;
    }

    public String getLabel() {
        return label;
    }

    public StatType getStatType() {
        return statType;
    }

    public int getAmount() {
        return amount;
    }

    public int getHpBonus() {
        return hpBonus;
    }

    public int getMpBonus() {
        return mpBonus;
    }

    public String getDescription() {
        if (hpBonus > 0) {
            return "HP+" + hpBonus;
        }
        if (mpBonus > 0) {
            return "MP+" + mpBonus;
        }
        return label + "+" + amount;
    }
}
