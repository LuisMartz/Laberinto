package game;

class Item {
    private final String name;
    private final EquipmentSlot slot;
    private final int strBonus;
    private final int defBonus;
    private final int agiBonus;
    private final int luckBonus;
    private final int hpBonus;
    private final int mpBonus;

    public Item(String name, EquipmentSlot slot, int strBonus, int defBonus, int agiBonus, int luckBonus, int hpBonus, int mpBonus) {
        this.name = name;
        this.slot = slot;
        this.strBonus = strBonus;
        this.defBonus = defBonus;
        this.agiBonus = agiBonus;
        this.luckBonus = luckBonus;
        this.hpBonus = hpBonus;
        this.mpBonus = mpBonus;
    }

    public String getName() {
        return name;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public int getStrBonus() {
        return strBonus;
    }

    public int getDefBonus() {
        return defBonus;
    }

    public int getAgiBonus() {
        return agiBonus;
    }

    public int getLuckBonus() {
        return luckBonus;
    }

    public int getHpBonus() {
        return hpBonus;
    }

    public int getMpBonus() {
        return mpBonus;
    }

    public String getBonusSummary() {
        StringBuilder summary = new StringBuilder();
        appendBonus(summary, "STR", strBonus);
        appendBonus(summary, "DEF", defBonus);
        appendBonus(summary, "AGI", agiBonus);
        appendBonus(summary, "LCK", luckBonus);
        appendBonus(summary, "HP", hpBonus);
        appendBonus(summary, "MP", mpBonus);
        if (summary.length() == 0) {
            return "No bonuses";
        }
        return summary.toString();
    }

    private void appendBonus(StringBuilder summary, String label, int value) {
        if (value == 0) {
            return;
        }
        if (summary.length() > 0) {
            summary.append(" ");
        }
        summary.append(label).append("+").append(value);
    }
}

