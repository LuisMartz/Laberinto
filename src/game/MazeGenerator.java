package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MazeGenerator {
    private static final int WALL = 0;
    private static final int FLOOR = 1;

    public int[][] generate(int requestedRows, int requestedColumns, Random random) {
        int rows = normalizeSize(requestedRows);
        int columns = normalizeSize(requestedColumns);
        int[][] maze = new int[rows][columns];
        carve(1, 1, maze, random);
        maze[1][1] = FLOOR;
        return maze;
    }

    private int normalizeSize(int value) {
        int size = Math.max(7, value);
        return size % 2 == 0 ? size + 1 : size;
    }

    private void carve(int row, int column, int[][] maze, Random random) {
        maze[row][column] = FLOOR;
        List<int[]> dirs = new ArrayList<>();
        dirs.add(new int[]{-2, 0});
        dirs.add(new int[]{2, 0});
        dirs.add(new int[]{0, -2});
        dirs.add(new int[]{0, 2});
        Collections.shuffle(dirs, random);

        for (int[] dir : dirs) {
            int nextRow = row + dir[0];
            int nextColumn = column + dir[1];
            if (!inside(nextRow, nextColumn, maze) || maze[nextRow][nextColumn] == FLOOR) {
                continue;
            }
            maze[row + dir[0] / 2][column + dir[1] / 2] = FLOOR;
            carve(nextRow, nextColumn, maze, random);
        }
    }

    private boolean inside(int row, int column, int[][] maze) {
        return row > 0 && row < maze.length - 1 && column > 0 && column < maze[0].length - 1;
    }
}
