package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class Inventory {
    private final List<Item> items = new ArrayList<>();
    private final EnumMap<EquipmentSlot, Item> equipped = new EnumMap<>(EquipmentSlot.class);

    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Map<EquipmentSlot, Item> getEquippedItems() {
        return Collections.unmodifiableMap(equipped);
    }

    public boolean equipItem(int index) {
        if (index < 0 || index >= items.size()) {
            return false;
        }
        Item item = items.remove(index);
        Item previous = equipped.put(item.getSlot(), item);
        if (previous != null) {
            items.add(previous);
        }
        return true;
    }

    public boolean unequipItem(EquipmentSlot slot) {
        Item item = equipped.remove(slot);
        if (item == null) {
            return false;
        }
        items.add(item);
        return true;
    }

    public int getBonusStr() {
        int total = 0;
        for (Item item : equipped.values()) {
            total += item.getStrBonus();
        }
        return total;
    }

    public int getBonusDef() {
        int total = 0;
        for (Item item : equipped.values()) {
            total += item.getDefBonus();
        }
        return total;
    }

    public int getBonusAgi() {
        int total = 0;
        for (Item item : equipped.values()) {
            total += item.getAgiBonus();
        }
        return total;
    }

    public int getBonusLuck() {
        int total = 0;
        for (Item item : equipped.values()) {
            total += item.getLuckBonus();
        }
        return total;
    }

    public int getBonusHp() {
        int total = 0;
        for (Item item : equipped.values()) {
            total += item.getHpBonus();
        }
        return total;
    }

    public int getBonusMp() {
        int total = 0;
        for (Item item : equipped.values()) {
            total += item.getMpBonus();
        }
        return total;
    }
}

