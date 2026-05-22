package game;

import java.util.Random;

public class LootFactory {
    private static final String[] PREFIXES = {
            "Rusty", "Sharp", "Heavy", "Traveler", "Iron", "Lucky", "Burning", "Frozen",
            "Vicious", "Sacred", "Ancient", "Arcane", "Brutal", "Silent", "Radiant"
    };
    private static final String[] MATERIALS = {
            "Copper", "Iron", "Steel", "Obsidian", "Silver", "Gold", "Moonlit", "Runed"
    };
    private static final String[] SUFFIXES = {
            "of Sparks", "of Guarding", "of Haste", "of Fortune", "of Vigor", "of Focus",
            "of Thorns", "of Dawn", "of the Deep", "of Resolve"
    };

    private final Random random = new Random();

    public LootDrop rollForEnemy(CombatEnemy enemy, int level, boolean boss) {
        int coins = rollCoins(level, boss);
        boolean dropsItem = boss || random.nextDouble() < Math.min(0.55, 0.22 + level * 0.03);
        Item item = null;
        ItemRarity rarity = null;
        if (dropsItem) {
            rarity = rollRarity(boss);
            item = createItem(level, rarity);
        }
        return new LootDrop(coins, item, rarity);
    }

    private int rollCoins(int level, boolean boss) {
        int base = boss ? 35 : 8;
        int spread = boss ? 28 : 12;
        return base + level * (boss ? 10 : 4) + random.nextInt(spread + 1);
    }

    private ItemRarity rollRarity(boolean boss) {
        double roll = random.nextDouble();
        double cumulative = 0;
        for (ItemRarity rarity : ItemRarity.values()) {
            cumulative += boss && rarity.ordinal() >= ItemRarity.RARE.ordinal()
                    ? rarity.getWeight() * 1.8
                    : rarity.getWeight();
            if (roll <= cumulative) {
                return rarity;
            }
        }
        return ItemRarity.COMMON;
    }

    private Item createItem(int level, ItemRarity rarity) {
        EquipmentSlot slot = randomSlot();
        int power = rarity.getPower() + Math.max(0, level / 2);
        WeaponScaling weaponScaling = randomWeaponScaling(slot);
        String name = rarity.getLabel() + " " + randomPart(PREFIXES) + " " + randomPart(MATERIALS) + " "
                + baseName(slot, weaponScaling) + " " + randomPart(SUFFIXES);

        int str = 0;
        int dex = 0;
        int intel = 0;
        int def = 0;
        int agi = 0;
        int luck = 0;
        int hp = 0;
        int mp = 0;

        switch (slot) {
            case WEAPON:
            case RIGHT_HAND:
                if (weaponScaling == WeaponScaling.STR) {
                    str = power + random.nextInt(2);
                } else {
                    dex = power + random.nextInt(2);
                    agi = rarity.getPower() >= 2 ? 1 : 0;
                }
                luck = rarity.getPower() >= 3 ? 1 : 0;
                break;
            case ARMOR:
            case HEAD:
                def = power;
                hp = power * 5;
                break;
            case BOOTS:
                agi = power;
                def = rarity.getPower() >= 3 ? 1 : 0;
                break;
            case RING:
            case ACCESSORY:
                if (random.nextBoolean()) {
                    luck = power;
                } else {
                    intel = power;
                }
                mp = power * 4;
                break;
            case LEFT_HAND:
            case GEAR:
                def = Math.max(1, power - 1);
                agi = rarity.getPower() >= 2 ? 1 : 0;
                hp = power * 3;
                break;
            default:
                luck = power;
                break;
        }

        if (rarity == ItemRarity.EPIC || rarity == ItemRarity.LEGENDARY) {
            str += random.nextBoolean() ? 1 : 0;
            dex += random.nextBoolean() ? 1 : 0;
            intel += random.nextBoolean() ? 1 : 0;
            def += random.nextBoolean() ? 1 : 0;
            agi += random.nextBoolean() ? 1 : 0;
            hp += rarity.getPower() * 3;
        }

        return new Item(name, slot, str, dex, intel, def, agi, luck, hp, mp, weaponScaling);
    }

    private EquipmentSlot randomSlot() {
        EquipmentSlot[] slots = EquipmentSlot.values();
        return slots[random.nextInt(slots.length)];
    }

    private String randomPart(String[] parts) {
        return parts[random.nextInt(parts.length)];
    }

    private WeaponScaling randomWeaponScaling(EquipmentSlot slot) {
        if (slot == EquipmentSlot.WEAPON || slot == EquipmentSlot.RIGHT_HAND) {
            return random.nextBoolean() ? WeaponScaling.STR : WeaponScaling.DEX;
        }
        return WeaponScaling.NONE;
    }

    private String baseName(EquipmentSlot slot, WeaponScaling weaponScaling) {
        switch (slot) {
            case WEAPON:
                return weaponScaling == WeaponScaling.DEX ? "Dagger" : "Blade";
            case ARMOR:
                return "Armor";
            case BOOTS:
                return "Boots";
            case HEAD:
                return "Helm";
            case RING:
                return "Ring";
            case LEFT_HAND:
            case RIGHT_HAND:
                return weaponScaling == WeaponScaling.DEX ? "Dagger" : "Gauntlet";
            case GEAR:
                return "Charm";
            case ACCESSORY:
                return "Pendant";
            default:
                return "Relic";
        }
    }
}
