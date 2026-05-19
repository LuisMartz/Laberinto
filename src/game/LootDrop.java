package game;

public class LootDrop {
    private final int coins;
    private final Item item;
    private final ItemRarity rarity;

    public LootDrop(int coins, Item item, ItemRarity rarity) {
        this.coins = coins;
        this.item = item;
        this.rarity = rarity;
    }

    public int getCoins() {
        return coins;
    }

    public Item getItem() {
        return item;
    }

    public ItemRarity getRarity() {
        return rarity;
    }

    public boolean hasItem() {
        return item != null;
    }
}
