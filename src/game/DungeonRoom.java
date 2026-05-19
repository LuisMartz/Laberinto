package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DungeonRoom {
    private final RoomType type;
    private final int[][] tiles;
    private final List<Enemy> enemies = new ArrayList<>();
    private boolean cleared;

    public DungeonRoom(RoomType type, int[][] tiles) {
        this.type = type;
        this.tiles = copyTiles(tiles);
    }

    public RoomType getType() {
        return type;
    }

    public int[][] getTiles() {
        return tiles;
    }

    public int getRows() {
        return tiles.length;
    }

    public int getColumns() {
        return tiles.length == 0 ? 0 : tiles[0].length;
    }

    public int getTile(int row, int column) {
        return tiles[row][column];
    }

    public void setTile(int row, int column, TileType type) {
        tiles[row][column] = type.getCode();
    }

    public boolean isWalkable(int row, int column) {
        return isInside(row, column) && getTile(row, column) != TileType.WALL.getCode();
    }

    public boolean isInside(int row, int column) {
        return row >= 0 && row < getRows() && column >= 0 && column < getColumns();
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    private int[][] copyTiles(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }
}
