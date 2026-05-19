package game;

public class Enemy {
    private final String name;
    private final boolean boss;
    private final CombatEnemy combatEnemy;
    private final GridPosition position;
    private boolean alive = true;

    public Enemy(String name, boolean boss, int row, int column) {
        this.name = name;
        this.boss = boss;
        this.combatEnemy = CombatEnemy.forLevel(1, boss);
        this.position = new GridPosition(row, column);
    }

    public Enemy(CombatEnemy combatEnemy, boolean boss, int row, int column) {
        this.name = combatEnemy.getName();
        this.boss = boss;
        this.combatEnemy = combatEnemy;
        this.position = new GridPosition(row, column);
    }

    public String getName() {
        return name;
    }

    public boolean isBoss() {
        return boss;
    }

    public boolean isAlive() {
        return alive;
    }

    public CombatEnemy getCombatEnemy() {
        return combatEnemy;
    }

    public void defeat() {
        alive = false;
        position.set(-1, -1);
    }

    public int getRow() {
        return position.getRow();
    }

    public int getColumn() {
        return position.getColumn();
    }

    public void setPosition(int row, int column) {
        position.set(row, column);
    }
}
