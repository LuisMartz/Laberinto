package game;

public class GridPosition {
    private int row;
    private int column;

    public GridPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void set(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
