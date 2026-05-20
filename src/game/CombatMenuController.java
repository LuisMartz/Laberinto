package game;

import java.util.ArrayList;
import java.util.List;

class CombatMenuController {
    private static final String[] MAIN_OPTIONS = {"Attack", "Skills", "Defend", "Items"};

    private final PlayerData playerData;
    private MenuState state = MenuState.MAIN;
    private String[] options = MAIN_OPTIONS;
    private int selectedIndex;
    private SkillCategory currentCategory = SkillCategory.ATTACK;
    private List<Skill> currentSkillList = new ArrayList<>();
    private List<ConsumableStack> currentItemList = new ArrayList<>();

    CombatMenuController(PlayerData playerData) {
        this.playerData = playerData;
    }

    MenuState getState() {
        return state;
    }

    String[] getOptions() {
        return options;
    }

    int getSelectedIndex() {
        return selectedIndex;
    }

    String getSelectedOption() {
        if (selectedIndex < 0 || selectedIndex >= options.length) {
            return "";
        }
        return options[selectedIndex];
    }

    void moveLeft() {
        selectedIndex = Math.max(0, selectedIndex - 1);
    }

    void moveRight() {
        selectedIndex = Math.min(options.length - 1, selectedIndex + 1);
    }

    void showMain() {
        setState(MenuState.MAIN);
    }

    void showCategories() {
        setState(MenuState.CATEGORY);
    }

    void showItems() {
        setState(MenuState.ITEM_LIST);
    }

    boolean chooseCategory() {
        if (isBackSelected()) {
            showMain();
            return false;
        }
        currentCategory = SkillCategory.values()[selectedIndex];
        currentSkillList = activeSkillsInCategory(currentCategory);
        if (currentSkillList.isEmpty()) {
            showMain();
            return false;
        }
        setState(MenuState.SKILL_LIST);
        return true;
    }

    boolean isBackSelected() {
        return selectedIndex == options.length - 1 && state != MenuState.MAIN;
    }

    Skill getSelectedSkill() {
        if (state != MenuState.SKILL_LIST || selectedIndex < 0 || selectedIndex >= currentSkillList.size()) {
            return null;
        }
        return currentSkillList.get(selectedIndex);
    }

    ConsumableStack getSelectedItem() {
        refreshConsumables();
        if (state != MenuState.ITEM_LIST || selectedIndex < 0 || selectedIndex >= currentItemList.size()) {
            return null;
        }
        return currentItemList.get(selectedIndex);
    }

    boolean isCurrentOptionEnabled() {
        return isOptionEnabled(selectedIndex);
    }

    boolean isOptionEnabled(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= options.length) {
            return false;
        }
        if (state == MenuState.MAIN) {
            String option = options[optionIndex];
            if (option.equals("Skills")) {
                return hasUsableSkills();
            }
            if (option.equals("Items")) {
                return playerData.hasConsumables();
            }
            return true;
        }
        if (state == MenuState.CATEGORY) {
            if (optionIndex == options.length - 1) {
                return true;
            }
            return hasActiveSkillsInCategory(SkillCategory.values()[optionIndex]);
        }
        if (state == MenuState.SKILL_LIST) {
            if (optionIndex == options.length - 1) {
                return true;
            }
            if (optionIndex >= currentSkillList.size()) {
                return false;
            }
            return currentSkillList.get(optionIndex).getMpCost() <= playerData.getCurrentMp();
        }
        if (state == MenuState.ITEM_LIST) {
            if (optionIndex == options.length - 1) {
                return true;
            }
            refreshConsumables();
            return optionIndex < currentItemList.size() && currentItemList.get(optionIndex).getCount() > 0;
        }
        return true;
    }

    boolean hasUsableSkills() {
        for (SkillCategory category : SkillCategory.values()) {
            if (hasActiveSkillsInCategory(category)) {
                return true;
            }
        }
        return false;
    }

    private void setState(MenuState state) {
        this.state = state;
        selectedIndex = 0;
        if (state == MenuState.MAIN) {
            options = MAIN_OPTIONS;
        } else if (state == MenuState.CATEGORY) {
            SkillCategory[] categories = SkillCategory.values();
            String[] nextOptions = new String[categories.length + 1];
            for (int i = 0; i < categories.length; i++) {
                nextOptions[i] = formatCategory(categories[i]);
            }
            nextOptions[categories.length] = "Back";
            options = nextOptions;
        } else if (state == MenuState.SKILL_LIST) {
            String[] nextOptions = new String[currentSkillList.size() + 1];
            for (int i = 0; i < currentSkillList.size(); i++) {
                Skill skill = currentSkillList.get(i);
                nextOptions[i] = skill.getName() + " (MP " + skill.getMpCost() + ")";
            }
            nextOptions[currentSkillList.size()] = "Back";
            options = nextOptions;
        } else {
            refreshConsumables();
            String[] nextOptions = new String[currentItemList.size() + 1];
            for (int i = 0; i < currentItemList.size(); i++) {
                ConsumableStack stack = currentItemList.get(i);
                nextOptions[i] = stack.getName() + " x" + stack.getCount();
            }
            nextOptions[currentItemList.size()] = "Back";
            options = nextOptions;
        }
    }

    private boolean hasActiveSkillsInCategory(SkillCategory category) {
        return !activeSkillsInCategory(category).isEmpty();
    }

    private List<Skill> activeSkillsInCategory(SkillCategory category) {
        List<Skill> active = new ArrayList<>();
        List<Skill> unlocked = playerData.getSkillTree().getUnlockedSkillsByCategory(category);
        for (Skill skill : unlocked) {
            if (skill.getType() == SkillType.ACTIVE) {
                active.add(skill);
            }
        }
        return active;
    }

    private void refreshConsumables() {
        currentItemList = playerData.getConsumables();
    }

    private String formatCategory(SkillCategory category) {
        switch (category) {
            case ATTACK:
                return "Attack Skills";
            case DEFENSE:
                return "Defense Skills";
            case OFFENSIVE_MAGIC:
                return "Offensive Magic";
            case DEFENSIVE_MAGIC:
                return "Defensive Magic";
            case SUPPORT_MAGIC:
                return "Support Magic";
            default:
                return "Skills";
        }
    }
}
