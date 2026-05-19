package game;

public enum ItemRarity {
    COMMON("Common", 1, 0.58),
    UNCOMMON("Uncommon", 2, 0.28),
    RARE("Rare", 3, 0.10),
    EPIC("Epic", 4, 0.035),
    LEGENDARY("Legendary", 5, 0.005);

    private final String label;
    private final int power;
    private final double weight;

    ItemRarity(String label, int power, double weight) {
        this.label = label;
        this.power = power;
        this.weight = weight;
    }

    public String getLabel() {
        return label;
    }

    public int getPower() {
        return power;
    }

    public double getWeight() {
        return weight;
    }
}
