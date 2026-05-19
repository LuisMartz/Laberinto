package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DungeonFloor {
    private final int floorNumber;
    private final List<DungeonRoom> rooms = new ArrayList<>();
    private int currentRoomIndex;

    public DungeonFloor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void addRoom(DungeonRoom room) {
        rooms.add(room);
    }

    public List<DungeonRoom> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public DungeonRoom getCurrentRoom() {
        if (rooms.isEmpty()) {
            throw new IllegalStateException("Dungeon floor has no rooms.");
        }
        return rooms.get(currentRoomIndex);
    }

    public void setCurrentRoomIndex(int currentRoomIndex) {
        if (currentRoomIndex < 0 || currentRoomIndex >= rooms.size()) {
            throw new IllegalArgumentException("Room index out of bounds: " + currentRoomIndex);
        }
        this.currentRoomIndex = currentRoomIndex;
    }
}
