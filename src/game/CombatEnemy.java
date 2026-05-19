package game;

public class CombatEnemy {
    private final String name;
    private final int maxHp;
    private final int str;
    private final int def;
    private final int agi;
    private final int luck;
    private final int xpReward;

    public CombatEnemy(String name, int maxHp, int str, int def, int agi, int luck, int xpReward) {
        this.name = name;
        this.maxHp = maxHp;
        this.str = str;
        this.def = def;
        this.agi = agi;
        this.luck = luck;
        this.xpReward = xpReward;
    }

    public static CombatEnemy forLevel(int level, boolean boss) {
        int scale = Math.max(1, level);
        if (boss) {
            return new CombatEnemy("Boss " + scale, 140 + scale * 24, 8 + scale * 2, 5 + scale, 4 + scale / 2, 4 + scale / 2, 70 + scale * 12);
        }
        return new CombatEnemy("Enemy " + scale, 90 + scale * 12, 5 + scale, 3 + scale / 2, 4 + scale / 3, 3 + scale / 3, 35 + scale * 6);
    }

    public String getName() {
        return name;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getStr() {
        return str;
    }

    public int getDef() {
        return def;
    }

    public int getAgi() {
        return agi;
    }

    public int getLuck() {
        return luck;
    }

    public int getXpReward() {
        return xpReward;
    }
}
