package game;

class Item {
    private final String name;
    private final EquipmentSlot slot;
    private final int strBonus;
    private final int dexBonus;
    private final int intBonus;
    private final int defBonus;
    private final int agiBonus;
    private final int luckBonus;
    private final int hpBonus;
    private final int mpBonus;
    private final WeaponScaling weaponScaling;

    public Item(String name, EquipmentSlot slot, int strBonus, int defBonus, int agiBonus, int luckBonus, int hpBonus, int mpBonus) {
        this(name, slot, strBonus, 0, 0, defBonus, agiBonus, luckBonus, hpBonus, mpBonus, defaultScaling(slot));
    }

    public Item(String name, EquipmentSlot slot, int strBonus, int dexBonus, int intBonus, int defBonus, int agiBonus,
                int luckBonus, int hpBonus, int mpBonus, WeaponScaling weaponScaling) {
        this.name = name;
        this.slot = slot;
        this.strBonus = strBonus;
        this.dexBonus = dexBonus;
        this.intBonus = intBonus;
        this.defBonus = defBonus;
        this.agiBonus = agiBonus;
        this.luckBonus = luckBonus;
        this.hpBonus = hpBonus;
        this.mpBonus = mpBonus;
        this.weaponScaling = weaponScaling == null ? WeaponScaling.NONE : weaponScaling;
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

    public int getDexBonus() {
        return dexBonus;
    }

    public int getIntBonus() {
        return intBonus;
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

    public WeaponScaling getWeaponScaling() {
        return weaponScaling;
    }

    public String getBonusSummary() {
        StringBuilder summary = new StringBuilder();
        appendBonus(summary, "STR", strBonus);
        appendBonus(summary, "DEX", dexBonus);
        appendBonus(summary, "INT", intBonus);
        appendBonus(summary, "DEF", defBonus);
        appendBonus(summary, "AGI", agiBonus);
        appendBonus(summary, "LCK", luckBonus);
        appendBonus(summary, "HP", hpBonus);
        appendBonus(summary, "MP", mpBonus);
        if (weaponScaling == WeaponScaling.DEX) {
            appendText(summary, "DEX weapon");
        } else if (weaponScaling == WeaponScaling.STR) {
            appendText(summary, "STR weapon");
        }
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

    private void appendText(StringBuilder summary, String text) {
        if (summary.length() > 0) {
            summary.append(" ");
        }
        summary.append(text);
    }

    private static WeaponScaling defaultScaling(EquipmentSlot slot) {
        return slot == EquipmentSlot.WEAPON || slot == EquipmentSlot.RIGHT_HAND ? WeaponScaling.STR : WeaponScaling.NONE;
    }
}

