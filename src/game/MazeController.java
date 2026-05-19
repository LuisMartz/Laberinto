package game;

import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class MazeController {
    public interface Listener {
        void onCombatRequested(CombatEnemy enemy);

        void onLevelExitRequested(int nextLevel);

        void onMazeChanged();
    }

    private final MazeState state;
    private final Listener listener;
    private final Random random = new Random();
    private int jumpDirection;

    public MazeController(MazeState state, Listener listener) {
        this.state = state;
        this.listener = listener;
    }

    public void handleKey(int key) {
        if (key == KeyEvent.VK_LEFT) {
            movePlayer(0, -1);
            jumpDirection = -1;
        } else if (key == KeyEvent.VK_RIGHT) {
            movePlayer(0, 1);
            jumpDirection = 1;
        } else if (key == KeyEvent.VK_UP) {
            movePlayer(-1, 0);
            jumpDirection = 0;
        } else if (key == KeyEvent.VK_DOWN) {
            movePlayer(1, 0);
            jumpDirection = 0;
        } else if (key == KeyEvent.VK_SPACE && state.getLevel() > 5 && jumpDirection != 0) {
            jump();
        }
        resolvePlayerTile();
        listener.onMazeChanged();
    }

    public void moveEnemy() {
        if (!state.isEnemyAlive()) {
            return;
        }
        GridPosition next = nextStepTowardPlayer();
        if (next == null || random.nextDouble() > aggression()) {
            next = randomEnemyStep();
        }
        if (next != null) {
            state.moveEnemyTo(next.getRow(), next.getColumn());
        }
        if (state.isPlayerTouchingEnemy()) {
            requestCombat();
        }
        listener.onMazeChanged();
    }

    private void movePlayer(int rowDelta, int columnDelta) {
        int row = state.getPlayerRow() + rowDelta;
        int column = state.getPlayerColumn() + columnDelta;
        if (state.isWalkable(row, column)) {
            state.setPlayerPosition(row, column);
        }
    }

    private void jump() {
        int wallColumn = state.getPlayerColumn() + jumpDirection;
        int targetColumn = state.getPlayerColumn() + 2 * jumpDirection;
        int row = state.getPlayerRow();
        if (state.inside(row, targetColumn)
                && state.getTile(row, wallColumn) == TileType.WALL.getCode()
                && state.isWalkable(row, targetColumn)) {
            state.setPlayerPosition(row, targetColumn);
            jumpDirection = 0;
        }
    }

    private void resolvePlayerTile() {
        state.collectCurrentTile();
        if (state.isPlayerTouchingEnemy()) {
            requestCombat();
        } else if (state.isOnExit()) {
            listener.onLevelExitRequested(state.getLevel() + 1);
        }
    }

    private void requestCombat() {
        state.savePlayerPosition();
        CombatEnemy enemy = state.getEnemy() == null ? CombatEnemy.forLevel(state.getLevel(), false) : state.getEnemy().getCombatEnemy();
        listener.onCombatRequested(enemy);
    }

    private double aggression() {
        return Math.min(0.9, 0.35 + state.getLevel() * 0.08);
    }

    private GridPosition randomEnemyStep() {
        Enemy enemy = state.getEnemy();
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int tries = 0; tries < dirs.length; tries++) {
            int[] dir = dirs[random.nextInt(dirs.length)];
            int row = enemy.getRow() + dir[0];
            int column = enemy.getColumn() + dir[1];
            if (state.isWalkable(row, column) && state.getTile(row, column) == TileType.FLOOR.getCode()) {
                return new GridPosition(row, column);
            }
        }
        return null;
    }

    private GridPosition nextStepTowardPlayer() {
        Enemy enemy = state.getEnemy();
        if (enemy == null) {
            return null;
        }
        int rows = state.getRows();
        int columns = state.getColumns();
        boolean[][] visited = new boolean[rows][columns];
        GridPosition[][] previous = new GridPosition[rows][columns];
        Queue<GridPosition> queue = new ArrayDeque<>();
        queue.add(new GridPosition(enemy.getRow(), enemy.getColumn()));
        visited[enemy.getRow()][enemy.getColumn()] = true;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            GridPosition current = queue.remove();
            if (current.getRow() == state.getPlayerRow() && current.getColumn() == state.getPlayerColumn()) {
                return firstStepFromEnemy(current, previous, enemy);
            }
            for (int[] dir : dirs) {
                int row = current.getRow() + dir[0];
                int column = current.getColumn() + dir[1];
                if (!state.inside(row, column) || visited[row][column] || !state.isWalkable(row, column)) {
                    continue;
                }
                visited[row][column] = true;
                previous[row][column] = current;
                queue.add(new GridPosition(row, column));
            }
        }
        return null;
    }

    private GridPosition firstStepFromEnemy(GridPosition target, GridPosition[][] previous, Enemy enemy) {
        GridPosition current = target;
        GridPosition parent = previous[current.getRow()][current.getColumn()];
        while (parent != null && !(parent.getRow() == enemy.getRow() && parent.getColumn() == enemy.getColumn())) {
            current = parent;
            parent = previous[current.getRow()][current.getColumn()];
        }
        return current;
    }
}
