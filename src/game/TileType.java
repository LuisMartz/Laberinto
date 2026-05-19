package game;

public enum TileType {
    WALL(0),
    FLOOR(1),
    COIN(2),
    EXIT(3),
    ENEMY(4);

    private final int code;

    TileType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TileType fromCode(int code) {
        for (TileType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return WALL;
    }
}
