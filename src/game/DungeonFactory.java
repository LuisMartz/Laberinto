package game;

public final class DungeonFactory {
    private DungeonFactory() {
    }

    public static DungeonFloor fromLegacyMaze(int floorNumber, int[][] tiles) {
        DungeonFloor floor = new DungeonFloor(floorNumber);
        DungeonRoom room = new DungeonRoom(RoomType.NORMAL, tiles);
        floor.addRoom(room);
        floor.setCurrentRoomIndex(0);
        return floor;
    }
}
