package game;

public class Combatant {
    private final String id;
    private final String name;
    private final boolean playerControlled;
    private final int maxHp;
    private final int maxMp;
    private final int str;
    private final int def;
    private final int agi;
    private final int luck;
    private int currentHp;
    private int currentMp;
    private boolean guarding;

    public Combatant(String id, String name, boolean playerControlled, int maxHp, int currentHp, int maxMp, int currentMp,
                     int str, int def, int agi, int luck) {
        this.id = id;
        this.name = name;
        this.playerControlled = playerControlled;
        this.maxHp = Math.max(1, maxHp);
        this.maxMp = Math.max(0, maxMp);
        this.str = str;
        this.def = def;
        this.agi = agi;
        this.luck = luck;
        this.currentHp = clamp(currentHp, 0, this.maxHp);
        this.currentMp = clamp(currentMp, 0, this.maxMp);
    }

    public static Combatant fromPlayerData(PlayerData playerData) {
        return new Combatant("player", "Player", true, playerData.getMaxHp(), playerData.getCurrentHp(),
                playerData.getMaxMp(), playerData.getCurrentMp(), playerData.getTotalStr(), playerData.getTotalDef(),
                playerData.getTotalAgi(), playerData.getTotalLuck());
    }

    public static Combatant fromEnemy(CombatEnemy enemy) {
        return new Combatant("enemy", enemy.getName(), false, enemy.getMaxHp(), enemy.getMaxHp(), 0, 0,
                enemy.getStr(), enemy.getDef(), enemy.getAgi(), enemy.getLuck());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPlayerControlled() {
        return playerControlled;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public int getCurrentMp() {
        return currentMp;
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

    public boolean isGuarding() {
        return guarding;
    }

    public void setGuarding(boolean guarding) {
        this.guarding = guarding;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public void applyDamage(int amount) {
        currentHp = clamp(currentHp - Math.max(0, amount), 0, maxHp);
    }

    public void heal(int amount) {
        currentHp = clamp(currentHp + Math.max(0, amount), 0, maxHp);
    }

    public boolean spendMp(int amount) {
        if (amount < 0 || currentMp < amount) {
            return false;
        }
        currentMp -= amount;
        return true;
    }

    public void restoreMp(int amount) {
        currentMp = clamp(currentMp + Math.max(0, amount), 0, maxMp);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
