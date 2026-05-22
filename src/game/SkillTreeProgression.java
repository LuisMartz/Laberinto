package game;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SkillTreeProgression {
    private static final int STAT_NODE_BASE_COST = 15;
    private static final int SKILL_NODE_BASE_COST = 25;
    private final Set<String> purchasedStatNodes = new HashSet<>();

    public boolean isStatNodePurchased(SkillCategory category, int index) {
        return purchasedStatNodes.contains(statNodeId(category, index));
    }

    public boolean canPurchaseStatNode(PlayerData playerData, SkillCategory category, int index) {
        if (isStatNodePurchased(category, index)) {
            return false;
        }
        if (index == 0) {
            return true;
        }
        List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
        return index - 1 < skills.size() && playerData.getSkillTree().isUnlocked(skills.get(index - 1).getId());
    }

    public boolean canUnlockSkill(PlayerData playerData, Skill skill, int index) {
        return isStatNodePurchased(skill.getCategory(), index)
                && playerData.getSkillPoints() > 0
                && playerData.getSkillTree().canUnlock(skill.getId());
    }

    public int statNodeCost(SkillCategory category, int index) {
        return STAT_NODE_BASE_COST + index * 5 + category.ordinal() * 2;
    }

    public int skillNodeCost(Skill skill, int index) {
        return SKILL_NODE_BASE_COST + index * 8 + skill.getMpCost();
    }

    public PurchaseResult purchaseSelected(PlayerData playerData, MazeState mazeState, SkillCategory category, int index) {
        List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
        if (index < 0 || index >= skills.size()) {
            return PurchaseResult.failed("No node selected.");
        }

        if (!isStatNodePurchased(category, index)) {
            int cost = statNodeCost(category, index);
            if (!canPurchaseStatNode(playerData, category, index)) {
                return PurchaseResult.failed("Previous node required.");
            }
            if (!mazeState.spendScore(cost)) {
                return PurchaseResult.failed("Not enough coins.");
            }
            purchasedStatNodes.add(statNodeId(category, index));
            applyStatReward(playerData, category, index);
            return PurchaseResult.success("Stat node purchased. -" + cost + " coins.");
        }

        Skill skill = skills.get(index);
        if (playerData.getSkillTree().isUnlocked(skill.getId())) {
            return PurchaseResult.failed("Skill already unlocked.");
        }
        int cost = skillNodeCost(skill, index);
        if (!canUnlockSkill(playerData, skill, index)) {
            return PurchaseResult.failed("Requirements missing.");
        }
        if (!mazeState.spendScore(cost)) {
            return PurchaseResult.failed("Not enough coins.");
        }
        if (!playerData.unlockSkill(skill.getId())) {
            return PurchaseResult.failed("Not enough skill points.");
        }
        return PurchaseResult.success(skill.getName() + " unlocked. -" + cost + " coins.");
    }

    public void save(Properties properties) {
        properties.setProperty("skillProgression.statNodes", String.join(",", purchasedStatNodes));
    }

    public void load(Properties properties) {
        purchasedStatNodes.clear();
        String raw = properties.getProperty("skillProgression.statNodes", "");
        if (raw.trim().isEmpty()) {
            return;
        }
        for (String id : raw.split(",")) {
            if (!id.trim().isEmpty()) {
                purchasedStatNodes.add(id.trim());
            }
        }
    }

    public void reapplyPurchasedStats(PlayerData playerData) {
        playerData.resetTreeBonuses();
        for (String id : purchasedStatNodes) {
            String[] parts = id.split(":");
            if (parts.length != 2) {
                continue;
            }
            try {
                SkillCategory category = SkillCategory.valueOf(parts[0]);
                int index = Integer.parseInt(parts[1]);
                applyStatReward(playerData, category, index);
            } catch (IllegalArgumentException ignored) {
                // Ignore unknown saved nodes from older or edited save files.
            }
        }
    }

    public StatReward statReward(SkillCategory category, int index) {
        switch (category) {
            case ATTACK:
                return index % 3 == 0
                        ? new StatReward("STR", StatType.STR, 1, 0, 0)
                        : (index % 3 == 1
                        ? new StatReward("DEX", StatType.DEX, 1, 0, 0)
                        : new StatReward("LCK", StatType.LUCK, 1, 0, 0));
            case DEFENSE:
                return index % 2 == 0
                        ? new StatReward("DEF", StatType.DEF, 1, 0, 0)
                        : new StatReward("HP", null, 0, 10, 0);
            case OFFENSIVE_MAGIC:
                return index % 2 == 0
                        ? new StatReward("INT", StatType.INT, 1, 0, 0)
                        : new StatReward("MP", null, 0, 0, 8);
            case DEFENSIVE_MAGIC:
                return index % 2 == 0
                        ? new StatReward("INT", StatType.INT, 1, 0, 0)
                        : new StatReward("MP", null, 0, 0, 8);
            case SUPPORT_MAGIC:
                return index % 2 == 0
                        ? new StatReward("HP", null, 0, 10, 0)
                        : new StatReward("AGI", StatType.AGI, 1, 0, 0);
            default:
                return new StatReward("+", StatType.STR, 1, 0, 0);
        }
    }

    private void applyStatReward(PlayerData playerData, SkillCategory category, int index) {
        StatReward reward = statReward(category, index);
        if (reward.getStatType() != null) {
            playerData.applyTreeStatBonus(reward.getStatType(), reward.getAmount());
        }
        if (reward.getHpBonus() > 0) {
            playerData.applyTreeHpBonus(reward.getHpBonus());
        }
        if (reward.getMpBonus() > 0) {
            playerData.applyTreeMpBonus(reward.getMpBonus());
        }
    }

    private String statNodeId(SkillCategory category, int index) {
        return category.name() + ":" + index;
    }
}
