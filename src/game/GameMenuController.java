package game;

class GameMenuController {
    private static final int TAB_COUNT = 4;
    private int tabIndex;
    private boolean sectionActive;
    private int inventorySelection;
    private int statsSelection;
    private String skillCursorNodeId = SkillTreeProgression.START_NODE_ID;

    int getTabIndex() {
        return tabIndex;
    }

    void setTabIndex(int tabIndex) {
        this.tabIndex = Math.max(0, Math.min(TAB_COUNT - 1, tabIndex));
        sectionActive = false;
        resetSelections();
    }

    void previousTab() {
        setTabIndex(tabIndex - 1);
    }

    void nextTab() {
        setTabIndex(tabIndex + 1);
    }

    boolean isSectionActive() {
        return sectionActive;
    }

    void enterSection() {
        sectionActive = true;
    }

    void exitSection() {
        sectionActive = false;
    }

    int getInventorySelection() {
        return inventorySelection;
    }

    void setInventorySelection(int inventorySelection, int itemCount) {
        int maxIndex = Math.max(0, itemCount - 1);
        this.inventorySelection = Math.max(0, Math.min(maxIndex, inventorySelection));
    }

    void previousInventoryItem() {
        inventorySelection = Math.max(0, inventorySelection - 1);
    }

    void nextInventoryItem(int itemCount) {
        setInventorySelection(inventorySelection + 1, itemCount);
    }

    void moveInventorySelection(int dx, int dy, int itemCount) {
        int columns = 5;
        int next = inventorySelection + dx + dy * columns;
        setInventorySelection(next, itemCount);
    }

    int getStatsSelection() {
        return statsSelection;
    }

    void previousStat() {
        statsSelection = Math.max(0, statsSelection - 1);
    }

    void nextStat() {
        statsSelection = Math.min(StatType.values().length - 1, statsSelection + 1);
    }

    StatType getSelectedStat() {
        return StatType.values()[statsSelection];
    }

    String getSkillCursorNodeId() {
        return skillCursorNodeId;
    }

    void setSkillCursorNodeId(String skillCursorNodeId) {
        this.skillCursorNodeId = skillCursorNodeId == null ? SkillTreeProgression.START_NODE_ID : skillCursorNodeId;
    }

    void moveSkillCursor(PlayerData playerData, SkillTreeProgression progression, int dx, int dy) {
        skillCursorNodeId = progression.neighborInDirection(playerData, skillCursorNodeId, dx, dy);
    }

    private void resetSelections() {
        statsSelection = 0;
        inventorySelection = 0;
    }
}
