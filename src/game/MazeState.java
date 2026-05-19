package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class MazeState {
    private static final int COINS_PER_LEVEL = 4;

    private final MazeGenerator generator = new MazeGenerator();
    private final Random random = new Random();
    private int[][] tiles;
    private int playerRow;
    private int playerColumn;
    private int savedPlayerRow;
    private int savedPlayerColumn;
    private int level = 1;
    private int score;
    private Enemy enemy;
    private boolean enemyAlive = true;

    public MazeState() {
        generateLevel(1);
    }

    public void generateLevel(int level) {
        this.level = Math.max(1, level);
        int rows = Math.min(21, 7 + this.level * 2);
        int columns = Math.min(25, 9 + this.level * 2);
        tiles = generator.generate(rows, columns, random);
        playerRow = 1;
        playerColumn = 1;
        enemyAlive = true;
        placeCoins();
        placeEnemy();
    }

    public int[][] getTiles() {
        return tiles;
    }

    public int getRows() {
        return tiles.length;
    }

    public int getColumns() {
        return tiles[0].length;
    }

    public int getTile(int row, int column) {
        return tiles[row][column];
    }

    public void setTile(int row, int column, TileType type) {
        tiles[row][column] = type.getCode();
    }

    public boolean isWalkable(int row, int column) {
        return inside(row, column) && getTile(row, column) != TileType.WALL.getCode();
    }

    public boolean inside(int row, int column) {
        return row >= 0 && row < getRows() && column >= 0 && column < getColumns();
    }

    public int getPlayerRow() {
        return playerRow;
    }

    public int getPlayerColumn() {
        return playerColumn;
    }

    public void setPlayerPosition(int row, int column) {
        playerRow = row;
        playerColumn = column;
    }

    public void savePlayerPosition() {
        savedPlayerRow = playerRow;
        savedPlayerColumn = playerColumn;
    }

    public void restorePlayerPosition() {
        playerRow = savedPlayerRow;
        playerColumn = savedPlayerColumn;
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        score += amount;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public boolean isEnemyAlive() {
        return enemyAlive && enemy != null && enemy.isAlive();
    }

    public void defeatEnemy() {
        if (enemy == null) {
            return;
        }
        clearEnemyTile();
        enemy.defeat();
        enemyAlive = false;
    }

    public void moveEnemyTo(int row, int column) {
        if (enemy == null || !enemyAlive) {
            return;
        }
        clearEnemyTile();
        enemy.setPosition(row, column);
        if (inside(row, column) && getTile(row, column) == TileType.FLOOR.getCode()) {
            setTile(row, column, TileType.ENEMY);
        }
    }

    public boolean isPlayerTouchingEnemy() {
        return isEnemyAlive() && enemy.getRow() == playerRow && enemy.getColumn() == playerColumn;
    }

    public boolean collectCurrentTile() {
        if (getTile(playerRow, playerColumn) != TileType.COIN.getCode()) {
            return false;
        }
        addScore(10);
        setTile(playerRow, playerColumn, TileType.FLOOR);
        if (!hasCoins()) {
            placeExit();
        }
        return true;
    }

    public boolean isOnExit() {
        return getTile(playerRow, playerColumn) == TileType.EXIT.getCode();
    }

    public boolean hasCoins() {
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getColumns(); column++) {
                if (getTile(row, column) == TileType.COIN.getCode()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<GridPosition> getFloorPositions() {
        List<GridPosition> positions = new ArrayList<>();
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getColumns(); column++) {
                if (getTile(row, column) == TileType.FLOOR.getCode()) {
                    positions.add(new GridPosition(row, column));
                }
            }
        }
        return positions;
    }

    public Properties toProperties() {
        Properties properties = new Properties();
        properties.setProperty("level", String.valueOf(level));
        properties.setProperty("score", String.valueOf(score));
        properties.setProperty("playerRow", String.valueOf(playerRow));
        properties.setProperty("playerColumn", String.valueOf(playerColumn));
        properties.setProperty("enemyRow", String.valueOf(enemy == null ? -1 : enemy.getRow()));
        properties.setProperty("enemyColumn", String.valueOf(enemy == null ? -1 : enemy.getColumn()));
        properties.setProperty("enemyAlive", String.valueOf(enemyAlive));
        properties.setProperty("rows", String.valueOf(getRows()));
        properties.setProperty("columns", String.valueOf(getColumns()));
        StringBuilder data = new StringBuilder();
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getColumns(); column++) {
                if (data.length() > 0) {
                    data.append(',');
                }
                data.append(getTile(row, column));
            }
        }
        properties.setProperty("tiles", data.toString());
        return properties;
    }

    public void load(Properties properties) {
        level = Integer.parseInt(properties.getProperty("level", "1"));
        score = Integer.parseInt(properties.getProperty("score", "0"));
        playerRow = Integer.parseInt(properties.getProperty("playerRow", "1"));
        playerColumn = Integer.parseInt(properties.getProperty("playerColumn", "1"));
        int rows = Integer.parseInt(properties.getProperty("rows", "7"));
        int columns = Integer.parseInt(properties.getProperty("columns", "7"));
        String[] values = properties.getProperty("tiles", "").split(",");
        tiles = new int[rows][columns];
        for (int i = 0; i < rows * columns && i < values.length; i++) {
            tiles[i / columns][i % columns] = Integer.parseInt(values[i]);
        }
        enemyAlive = Boolean.parseBoolean(properties.getProperty("enemyAlive", "true"));
        int enemyRow = Integer.parseInt(properties.getProperty("enemyRow", "-1"));
        int enemyColumn = Integer.parseInt(properties.getProperty("enemyColumn", "-1"));
        enemy = new Enemy(CombatEnemy.forLevel(level, false), false, enemyRow, enemyColumn);
    }

    private void placeCoins() {
        int placed = 0;
        while (placed < COINS_PER_LEVEL) {
            GridPosition position = randomFloorAwayFromPlayer();
            if (position == null) {
                return;
            }
            setTile(position.getRow(), position.getColumn(), TileType.COIN);
            placed++;
        }
    }

    private void placeEnemy() {
        GridPosition position = randomFloorAwayFromPlayer();
        if (position == null) {
            enemy = null;
            enemyAlive = false;
            return;
        }
        enemy = new Enemy(CombatEnemy.forLevel(level, false), false, position.getRow(), position.getColumn());
        setTile(position.getRow(), position.getColumn(), TileType.ENEMY);
    }

    private void placeExit() {
        GridPosition position = farthestFloorFromPlayer();
        if (position != null) {
            setTile(position.getRow(), position.getColumn(), TileType.EXIT);
        }
    }

    private GridPosition randomFloorAwayFromPlayer() {
        List<GridPosition> positions = getFloorPositions();
        positions.removeIf(p -> Math.abs(p.getRow() - playerRow) + Math.abs(p.getColumn() - playerColumn) < 4);
        if (positions.isEmpty()) {
            return null;
        }
        return positions.get(random.nextInt(positions.size()));
    }

    private GridPosition farthestFloorFromPlayer() {
        GridPosition best = null;
        int bestDistance = -1;
        for (GridPosition position : getFloorPositions()) {
            int distance = Math.abs(position.getRow() - playerRow) + Math.abs(position.getColumn() - playerColumn);
            if (distance > bestDistance) {
                bestDistance = distance;
                best = position;
            }
        }
        return best;
    }

    private void clearEnemyTile() {
        if (enemy == null) {
            return;
        }
        int row = enemy.getRow();
        int column = enemy.getColumn();
        if (inside(row, column) && getTile(row, column) == TileType.ENEMY.getCode()) {
            setTile(row, column, TileType.FLOOR);
        }
    }
}
