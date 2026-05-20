package game;

class GameMenuController {
    private static final int TAB_COUNT = 4;
    private int tabIndex;
    private int inventorySelection;
    private int statsSelection;
    private int skillCategoryIndex;
    private int skillSelectionIndex;

    int getTabIndex() {
        return tabIndex;
    }

    void setTabIndex(int tabIndex) {
        this.tabIndex = Math.max(0, Math.min(TAB_COUNT - 1, tabIndex));
        resetSelections();
    }

    void previousTab() {
        setTabIndex(tabIndex - 1);
    }

    void nextTab() {
        setTabIndex(tabIndex + 1);
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

    int getSkillCategoryIndex() {
        return skillCategoryIndex;
    }

    int getSkillSelectionIndex() {
        return skillSelectionIndex;
    }

    SkillCategory getCurrentSkillCategory() {
        SkillCategory[] categories = SkillCategory.values();
        if (skillCategoryIndex < 0 || skillCategoryIndex >= categories.length) {
            return SkillCategory.ATTACK;
        }
        return categories[skillCategoryIndex];
    }

    void previousSkill() {
        skillSelectionIndex = Math.max(0, skillSelectionIndex - 1);
    }

    void nextSkill(PlayerData playerData) {
        int maxIndex = Math.max(0, playerData.getSkillTree().getSkillsByCategory(getCurrentSkillCategory()).size() - 1);
        skillSelectionIndex = Math.min(maxIndex, skillSelectionIndex + 1);
    }

    void previousSkillCategory(PlayerData playerData) {
        skillCategoryIndex = Math.max(0, skillCategoryIndex - 1);
        clampSkillSelection(playerData);
    }

    void nextSkillCategory(PlayerData playerData) {
        skillCategoryIndex = Math.min(SkillCategory.values().length - 1, skillCategoryIndex + 1);
        clampSkillSelection(playerData);
    }

    private void resetSelections() {
        statsSelection = 0;
        inventorySelection = 0;
        skillSelectionIndex = 0;
    }

    private void clampSkillSelection(PlayerData playerData) {
        int maxIndex = Math.max(0, playerData.getSkillTree().getSkillsByCategory(getCurrentSkillCategory()).size() - 1);
        skillSelectionIndex = Math.min(skillSelectionIndex, maxIndex);
    }
}
