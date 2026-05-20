package game;

import java.util.HashSet;
import java.util.Set;

public class GameProgress {
    private String worldId = "world_1";
    private String areaId = "area_1";
    private int floor = 1;
    private final Set<String> clearedAreas = new HashSet<>();

    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = Math.max(1, floor);
    }

    public void markAreaCleared(String areaId) {
        clearedAreas.add(areaId);
    }

    public boolean isAreaCleared(String areaId) {
        return clearedAreas.contains(areaId);
    }
}
