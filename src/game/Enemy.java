package game;

public class Enemy {
    private final String name;
    private final boolean boss;
    private final GridPosition position;
    private boolean alive = true;

    public Enemy(String name, boolean boss, int row, int column) {
        this.name = name;
        this.boss = boss;
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
