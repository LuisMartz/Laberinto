package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class PlayerData {
    private int baseStr = 5;
    private int baseDef = 5;
    private int baseAgi = 5;
    private int baseLuck = 5;
    private int baseMind = 5;
    private int baseCon = 5;
    private int baseHp = 100;
    private int baseMp = 50;
    private int treeBonusStr = 0;
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
        inventory.addItem(new Item("Rusty Sword", EquipmentSlot.WEAPON, 2, 0, 0, 0, 0, 0));
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

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void clampVitals() {
        currentHp = clamp(currentHp, 0, getMaxHp());
        currentMp = clamp(currentMp, 0, getMaxMp());
    }
}
