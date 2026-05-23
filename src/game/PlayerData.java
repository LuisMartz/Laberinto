package game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PlayerData {
    private int baseStr = 5;
    private int baseDex = 5;
    private int baseInt = 5;
    private int baseDef = 5;
    private int baseAgi = 5;
    private int baseLuck = 5;
    private int baseMind = 5;
    private int baseCon = 5;
    private int baseHp = 100;
    private int baseMp = 50;
    private int treeBonusStr = 0;
    private int treeBonusDex = 0;
    private int treeBonusInt = 0;
    private int treeBonusDef = 0;
    private int treeBonusAgi = 0;
    private int treeBonusLuck = 0;
    private int treeBonusMind = 0;
    private int treeBonusCon = 0;
    private int treeBonusHp = 0;
    private int treeBonusMp = 0;
    private int currentHp;
    private int currentMp;

    private int level = 1;
    private int currentXp = 0;
    private int statPoints = 0;
    private int skillPoints = 0;

    private final Inventory inventory = new Inventory();
    private final LinkedHashMap<String, Integer> consumables = new LinkedHashMap<>();
    private final SkillTree skillTree = new SkillTree();

    public PlayerData() {
        currentHp = getMaxHp();
        currentMp = getMaxMp();
        seedStarterItems();
    }

    private void seedStarterItems() {
        inventory.addItem(new Item("Rusty Sword", EquipmentSlot.WEAPON, 2, 0, 0, 0, 0, 0, 0, 0, WeaponScaling.STR));
        inventory.addItem(new Item("Training Dagger", EquipmentSlot.RIGHT_HAND, 0, 2, 0, 0, 1, 0, 0, 0, WeaponScaling.DEX));
        inventory.addItem(new Item("Leather Armor", EquipmentSlot.ARMOR, 0, 2, 0, 0, 10, 0));
        inventory.addItem(new Item("Traveler Boots", EquipmentSlot.BOOTS, 0, 0, 1, 0, 0, 0));
        inventory.addItem(new Item("Lucky Ring", EquipmentSlot.RING, 0, 0, 0, 1, 0, 0));
        inventory.addItem(new Item("Iron Helm", EquipmentSlot.HEAD, 0, 1, 0, 0, 5, 0));
        inventory.equipItem(0);
        inventory.equipItem(0);
        inventory.equipItem(0);
        addConsumable("Potion", 3);
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentXp() {
        return currentXp;
    }

    public int getNextLevelXp() {
        return 50 + (level - 1) * 25;
    }

    public int getStatPoints() {
        return statPoints;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public boolean addExperience(int amount) {
        boolean leveledUp = false;
        currentXp += amount;
        while (currentXp >= getNextLevelXp()) {
            currentXp -= getNextLevelXp();
            level++;
            statPoints += 3;
            skillPoints += 1;
            currentHp = getMaxHp();
            currentMp = getMaxMp();
            leveledUp = true;
        }
        return leveledUp;
    }

    public boolean allocateStatPoint(StatType statType) {
        if (statPoints <= 0) {
            return false;
        }
        switch (statType) {
            case STR:
                baseStr++;
                break;
            case DEX:
                baseDex++;
                break;
            case INT:
                baseInt++;
                break;
            case DEF:
                baseDef++;
                break;
            case AGI:
                baseAgi++;
                break;
            case LUCK:
                baseLuck++;
                break;
            case MIND:
                baseMind++;
                break;
            case CON:
                baseCon++;
                break;
            default:
                return false;
        }
        statPoints--;
        currentHp = clamp(currentHp, 0, getMaxHp());
        currentMp = clamp(currentMp, 0, getMaxMp());
        return true;
    }

    public boolean unlockSkill(String skillId) {
        if (skillPoints <= 0) {
            return false;
        }
        if (skillTree.unlock(skillId)) {
            skillPoints--;
            return true;
        }
        return false;
    }

    public void applyTreeStatBonus(StatType statType, int amount) {
        if (amount <= 0) {
            return;
        }
        switch (statType) {
            case STR:
                treeBonusStr += amount;
                break;
            case DEX:
                treeBonusDex += amount;
                break;
            case INT:
                treeBonusInt += amount;
                break;
            case DEF:
                treeBonusDef += amount;
                break;
            case AGI:
                treeBonusAgi += amount;
                break;
            case LUCK:
                treeBonusLuck += amount;
                break;
            case MIND:
                treeBonusMind += amount;
                break;
            case CON:
                treeBonusCon += amount;
                break;
            default:
                return;
        }
        currentHp = clamp(currentHp, 0, getMaxHp());
        currentMp = clamp(currentMp, 0, getMaxMp());
    }

    public void resetTreeBonuses() {
        treeBonusStr = 0;
        treeBonusDex = 0;
        treeBonusInt = 0;
        treeBonusDef = 0;
        treeBonusAgi = 0;
        treeBonusLuck = 0;
        treeBonusMind = 0;
        treeBonusCon = 0;
        treeBonusHp = 0;
        treeBonusMp = 0;
        currentHp = clamp(currentHp, 0, getMaxHp());
        currentMp = clamp(currentMp, 0, getMaxMp());
    }

    public void applyTreeHpBonus(int amount) {
        if (amount <= 0) {
            return;
        }
        treeBonusHp += amount;
        currentHp = clamp(currentHp + amount, 0, getMaxHp());
    }

    public void applyTreeMpBonus(int amount) {
        if (amount <= 0) {
            return;
        }
        treeBonusMp += amount;
        currentMp = clamp(currentMp + amount, 0, getMaxMp());
    }

    public SkillTree getSkillTree() {
        return skillTree;
    }

    public int getBaseStr() {
        return baseStr;
    }

    public int getBaseDex() {
        return baseDex;
    }

    public int getBaseInt() {
        return baseInt;
    }

    public int getBaseDef() {
        return baseDef;
    }

    public int getBaseAgi() {
        return baseAgi;
    }

    public int getBaseLuck() {
        return baseLuck;
    }

    public int getBaseMind() {
        return baseMind;
    }

    public int getBaseCon() {
        return baseCon;
    }

    public int getTotalStr() {
        return baseStr + treeBonusStr + inventory.getBonusStr() + skillTree.getPassiveBonusStr();
    }

    public int getTotalDex() {
        return baseDex + treeBonusDex + inventory.getBonusDex();
    }

    public int getTotalInt() {
        return baseInt + treeBonusInt + inventory.getBonusInt();
    }

    public int getTotalDef() {
        return baseDef + treeBonusDef + inventory.getBonusDef() + skillTree.getPassiveBonusDef();
    }

    public int getTotalAgi() {
        return baseAgi + treeBonusAgi + inventory.getBonusAgi() + skillTree.getPassiveBonusAgi();
    }

    public int getTotalLuck() {
        return baseLuck + treeBonusLuck + inventory.getBonusLuck() + skillTree.getPassiveBonusLuck();
    }

    public int getTotalMind() {
        return baseMind + treeBonusMind + skillTree.getPassiveBonusMp() / 5;
    }

    public int getTotalCon() {
        return baseCon + treeBonusCon + skillTree.getPassiveBonusHp() / 10;
    }

    public int getMaxHp() {
        return baseHp + getTotalCon() * 10 + treeBonusHp + inventory.getBonusHp() + skillTree.getPassiveBonusHp();
    }

    public int getMaxMp() {
        return baseMp + getTotalMind() * 5 + treeBonusMp + inventory.getBonusMp() + skillTree.getPassiveBonusMp();
    }

    public int getWeaponAttackStat() {
        return inventory.getWeaponScaling() == WeaponScaling.DEX ? getTotalDex() : getTotalStr();
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getCurrentMp() {
        return currentMp;
    }

    public void setCurrentHp(int value) {
        currentHp = clamp(value, 0, getMaxHp());
    }

    public void setCurrentMp(int value) {
        currentMp = clamp(value, 0, getMaxMp());
    }

    public boolean equipItem(int index) {
        boolean equipped = inventory.equipItem(index);
        if (equipped) {
            clampVitals();
        }
        return equipped;
    }

    public boolean unequipItem(EquipmentSlot slot) {
        boolean unequipped = inventory.unequipItem(slot);
        if (unequipped) {
            clampVitals();
        }
        return unequipped;
    }

    public void addItem(Item item) {
        inventory.addItem(item);
    }

    public List<Item> getInventoryItems() {
        return inventory.getItems();
    }

    public Map<EquipmentSlot, Item> getEquippedItems() {
        return inventory.getEquippedItems();
    }

    public void addConsumable(String name, int count) {
        if (count <= 0) {
            return;
        }
        consumables.put(name, consumables.getOrDefault(name, 0) + count);
    }

    public boolean hasConsumables() {
        for (int count : consumables.values()) {
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    public List<ConsumableStack> getConsumables() {
        List<ConsumableStack> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : consumables.entrySet()) {
            if (entry.getValue() > 0) {
                list.add(new ConsumableStack(entry.getKey(), entry.getValue()));
            }
        }
        return list;
    }

    public boolean useConsumable(String name) {
        Integer count = consumables.get(name);
        if (count == null || count <= 0) {
            return false;
        }
        consumables.put(name, count - 1);
        return true;
    }

    public void save(Properties properties) {
        properties.setProperty("player.level", String.valueOf(level));
        properties.setProperty("player.currentXp", String.valueOf(currentXp));
        properties.setProperty("player.statPoints", String.valueOf(statPoints));
        properties.setProperty("player.skillPoints", String.valueOf(skillPoints));
        properties.setProperty("player.baseStr", String.valueOf(baseStr));
        properties.setProperty("player.baseDex", String.valueOf(baseDex));
        properties.setProperty("player.baseInt", String.valueOf(baseInt));
        properties.setProperty("player.baseDef", String.valueOf(baseDef));
        properties.setProperty("player.baseAgi", String.valueOf(baseAgi));
        properties.setProperty("player.baseLuck", String.valueOf(baseLuck));
        properties.setProperty("player.baseMind", String.valueOf(baseMind));
        properties.setProperty("player.baseCon", String.valueOf(baseCon));
        properties.setProperty("player.currentHp", String.valueOf(currentHp));
        properties.setProperty("player.currentMp", String.valueOf(currentMp));

        List<Item> inventoryItems = inventory.getItems();
        properties.setProperty("player.inventory.count", String.valueOf(inventoryItems.size()));
        for (int i = 0; i < inventoryItems.size(); i++) {
            saveItem(properties, "player.inventory." + i + ".", inventoryItems.get(i));
        }

        List<Map.Entry<EquipmentSlot, Item>> equippedItems = new ArrayList<>(inventory.getEquippedItems().entrySet());
        properties.setProperty("player.equipped.count", String.valueOf(equippedItems.size()));
        for (int i = 0; i < equippedItems.size(); i++) {
            Map.Entry<EquipmentSlot, Item> entry = equippedItems.get(i);
            properties.setProperty("player.equipped." + i + ".slot", entry.getKey().name());
            saveItem(properties, "player.equipped." + i + ".item.", entry.getValue());
        }

        List<ConsumableStack> consumableStacks = getConsumables();
        properties.setProperty("player.consumables.count", String.valueOf(consumableStacks.size()));
        for (int i = 0; i < consumableStacks.size(); i++) {
            ConsumableStack stack = consumableStacks.get(i);
            properties.setProperty("player.consumables." + i + ".name", stack.getName());
            properties.setProperty("player.consumables." + i + ".count", String.valueOf(stack.getCount()));
        }

        skillTree.save(properties);
    }

    public void load(Properties properties) {
        level = getInt(properties, "player.level", level);
        currentXp = getInt(properties, "player.currentXp", currentXp);
        statPoints = getInt(properties, "player.statPoints", statPoints);
        skillPoints = getInt(properties, "player.skillPoints", skillPoints);
        baseStr = getInt(properties, "player.baseStr", baseStr);
        baseDex = getInt(properties, "player.baseDex", baseDex);
        baseInt = getInt(properties, "player.baseInt", baseInt);
        baseDef = getInt(properties, "player.baseDef", baseDef);
        baseAgi = getInt(properties, "player.baseAgi", baseAgi);
        baseLuck = getInt(properties, "player.baseLuck", baseLuck);
        baseMind = getInt(properties, "player.baseMind", baseMind);
        baseCon = getInt(properties, "player.baseCon", baseCon);

        resetTreeBonuses();
        skillTree.load(properties);
        loadInventory(properties);
        loadConsumables(properties);

        currentHp = getInt(properties, "player.currentHp", currentHp);
        currentMp = getInt(properties, "player.currentMp", currentMp);
        clampVitals();
    }

    private void loadInventory(Properties properties) {
        if (!properties.containsKey("player.inventory.count") && !properties.containsKey("player.equipped.count")) {
            return;
        }
        inventory.clear();
        int inventoryCount = getInt(properties, "player.inventory.count", 0);
        for (int i = 0; i < inventoryCount; i++) {
            Item item = loadItem(properties, "player.inventory." + i + ".");
            if (item != null) {
                inventory.addItem(item);
            }
        }

        int equippedCount = getInt(properties, "player.equipped.count", 0);
        for (int i = 0; i < equippedCount; i++) {
            Item item = loadItem(properties, "player.equipped." + i + ".item.");
            if (item != null) {
                inventory.equipDirect(item);
            }
        }
    }

    private void loadConsumables(Properties properties) {
        if (!properties.containsKey("player.consumables.count")) {
            return;
        }
        consumables.clear();
        int consumableCount = getInt(properties, "player.consumables.count", 0);
        for (int i = 0; i < consumableCount; i++) {
            String name = properties.getProperty("player.consumables." + i + ".name", "").trim();
            int count = getInt(properties, "player.consumables." + i + ".count", 0);
            if (!name.isEmpty() && count > 0) {
                addConsumable(name, count);
            }
        }
    }

    private void saveItem(Properties properties, String prefix, Item item) {
        properties.setProperty(prefix + "name", item.getName());
        properties.setProperty(prefix + "slot", item.getSlot().name());
        properties.setProperty(prefix + "str", String.valueOf(item.getStrBonus()));
        properties.setProperty(prefix + "dex", String.valueOf(item.getDexBonus()));
        properties.setProperty(prefix + "int", String.valueOf(item.getIntBonus()));
        properties.setProperty(prefix + "def", String.valueOf(item.getDefBonus()));
        properties.setProperty(prefix + "agi", String.valueOf(item.getAgiBonus()));
        properties.setProperty(prefix + "luck", String.valueOf(item.getLuckBonus()));
        properties.setProperty(prefix + "hp", String.valueOf(item.getHpBonus()));
        properties.setProperty(prefix + "mp", String.valueOf(item.getMpBonus()));
        properties.setProperty(prefix + "scaling", item.getWeaponScaling().name());
    }

    private Item loadItem(Properties properties, String prefix) {
        String name = properties.getProperty(prefix + "name");
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        EquipmentSlot slot = getEnum(properties, prefix + "slot", EquipmentSlot.class, EquipmentSlot.ACCESSORY);
        WeaponScaling scaling = getEnum(properties, prefix + "scaling", WeaponScaling.class, WeaponScaling.NONE);
        return new Item(
                name,
                slot,
                getInt(properties, prefix + "str", 0),
                getInt(properties, prefix + "dex", 0),
                getInt(properties, prefix + "int", 0),
                getInt(properties, prefix + "def", 0),
                getInt(properties, prefix + "agi", 0),
                getInt(properties, prefix + "luck", 0),
                getInt(properties, prefix + "hp", 0),
                getInt(properties, prefix + "mp", 0),
                scaling
        );
    }

    private int getInt(Properties properties, String key, int fallback) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private <T extends Enum<T>> T getEnum(Properties properties, String key, Class<T> type, T fallback) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, raw.trim());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void clampVitals() {
        currentHp = clamp(currentHp, 0, getMaxHp());
        currentMp = clamp(currentMp, 0, getMaxMp());
    }
}
