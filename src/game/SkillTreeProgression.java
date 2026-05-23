package game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SkillTreeProgression {
    public static final String START_NODE_ID = "START";
    private static final int STAT_NODE_BASE_COST = 15;
    private static final int SKILL_NODE_BASE_COST = 25;
    private static final int TRAVEL_COST = 8;
    private final Set<String> purchasedStatNodes = new HashSet<>();
    private String currentNodeId = START_NODE_ID;

    public boolean isStatNodePurchased(SkillCategory category, int index) {
        return purchasedStatNodes.contains(statNodeId(category, index))
                || purchasedStatNodes.contains(legacyStatNodeId(category, index));
    }

    public boolean canPurchaseStatNode(PlayerData playerData, SkillCategory category, int index) {
        if (isStatNodePurchased(category, index)) {
            return false;
        }
        return isAdjacentToCurrent(statNodeId(category, index), playerData);
    }

    public boolean canUnlockSkill(PlayerData playerData, Skill skill, int index) {
        return isAdjacentToCurrent(skillNodeId(skill), playerData)
                && playerData.getSkillPoints() > 0
                && playerData.getSkillTree().canUnlock(skill.getId());
    }

    public int statNodeCost(SkillCategory category, int index) {
        return STAT_NODE_BASE_COST + index * 5 + category.ordinal() * 2;
    }

    public int skillNodeCost(Skill skill, int index) {
        return SKILL_NODE_BASE_COST + index * 8 + skill.getMpCost();
    }

    public int travelCost() {
        return TRAVEL_COST;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public PurchaseResult activateSelected(PlayerData playerData, MazeState mazeState, String selectedNodeId) {
        if (selectedNodeId == null || selectedNodeId.trim().isEmpty()) {
            return PurchaseResult.failed("No node selected.");
        }
        if (selectedNodeId.equals(currentNodeId)) {
            return PurchaseResult.failed("Choose an adjacent node.");
        }
        if (!isAdjacentToCurrent(selectedNodeId, playerData)) {
            return PurchaseResult.failed("Node is not adjacent.");
        }

        if (selectedNodeId.startsWith("CAT:") || selectedNodeId.equals(START_NODE_ID)) {
            return moveToTravelNode(mazeState, selectedNodeId);
        }

        if (selectedNodeId.startsWith("STAT:")) {
            NodeRef ref = parseProgressionNode(selectedNodeId);
            if (ref == null) {
                return PurchaseResult.failed("Invalid stat node.");
            }
            return purchaseStatNode(playerData, mazeState, ref.category, ref.index);
        }

        if (selectedNodeId.startsWith("SKILL:")) {
            Skill skill = playerData.getSkillTree().getSkill(selectedNodeId.substring("SKILL:".length()));
            if (skill == null) {
                return PurchaseResult.failed("Invalid skill node.");
            }
            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(skill.getCategory());
            int index = skills.indexOf(skill);
            return unlockSkillNode(playerData, mazeState, skill, index);
        }

        return PurchaseResult.failed("Invalid node.");
    }

    public PurchaseResult purchaseSelected(PlayerData playerData, MazeState mazeState, SkillCategory category, int index) {
        List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
        if (index < 0 || index >= skills.size()) {
            return PurchaseResult.failed("No node selected.");
        }
        return activateSelected(playerData, mazeState, skillNodeId(skills.get(index)));
    }

    private PurchaseResult purchaseStatNode(PlayerData playerData, MazeState mazeState, SkillCategory category, int index) {
        if (!isStatNodePurchased(category, index)) {
            int cost = statNodeCost(category, index);
            if (!canPurchaseStatNode(playerData, category, index)) {
                return PurchaseResult.failed("Stat node is not adjacent.");
            }
            if (!mazeState.spendScore(cost)) {
                return PurchaseResult.failed("Not enough coins.");
            }
            purchasedStatNodes.add(statNodeId(category, index));
            applyStatReward(playerData, category, index);
            currentNodeId = statNodeId(category, index);
            return PurchaseResult.success("Stat node purchased. -" + cost + " coins.");
        }

        return moveToTravelNode(mazeState, statNodeId(category, index));
    }

    private PurchaseResult unlockSkillNode(PlayerData playerData, MazeState mazeState, Skill skill, int index) {
        if (playerData.getSkillTree().isUnlocked(skill.getId())) {
            return moveToTravelNode(mazeState, skillNodeId(skill));
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
        currentNodeId = skillNodeId(skill);
        return PurchaseResult.success(skill.getName() + " unlocked. -" + cost + " coins.");
    }

    private PurchaseResult moveToTravelNode(MazeState mazeState, String nodeId) {
        if (!mazeState.spendScore(TRAVEL_COST)) {
            return PurchaseResult.failed("Not enough coins to move.");
        }
        currentNodeId = nodeId;
        return PurchaseResult.success("Moved. -" + TRAVEL_COST + " coins.");
    }

    public void save(Properties properties) {
        properties.setProperty("skillProgression.statNodes", String.join(",", purchasedStatNodes));
        properties.setProperty("skillProgression.currentNode", currentNodeId);
    }

    public void load(Properties properties) {
        purchasedStatNodes.clear();
        currentNodeId = properties.getProperty("skillProgression.currentNode", START_NODE_ID);
        String raw = properties.getProperty("skillProgression.statNodes", "");
        if (raw.trim().isEmpty()) {
            return;
        }
        for (String id : raw.split(",")) {
            if (!id.trim().isEmpty()) {
                NodeRef ref = parseProgressionNode(id.trim());
                purchasedStatNodes.add(ref == null ? id.trim() : statNodeId(ref.category, ref.index));
            }
        }
    }

    public String neighborInDirection(PlayerData playerData, String fromNodeId, int dx, int dy) {
        Map<String, Point> positions = buildNodePositions(playerData);
        Map<String, List<String>> graph = buildGraph(playerData);
        Point from = positions.get(fromNodeId);
        if (from == null) {
            return START_NODE_ID;
        }
        String bestNode = fromNodeId;
        double bestScore = Double.MAX_VALUE;
        for (String candidate : graph.getOrDefault(fromNodeId, new ArrayList<>())) {
            Point to = positions.get(candidate);
            if (to == null) {
                continue;
            }
            int vx = to.x - from.x;
            int vy = to.y - from.y;
            if (dx != 0 && Integer.signum(vx) != dx) {
                continue;
            }
            if (dy != 0 && Integer.signum(vy) != dy) {
                continue;
            }
            double distance = from.distanceSq(to);
            double alignmentPenalty = dx != 0 ? Math.abs(vy) * 10.0 : Math.abs(vx) * 10.0;
            double score = distance + alignmentPenalty;
            if (score < bestScore) {
                bestScore = score;
                bestNode = candidate;
            }
        }
        return bestNode;
    }

    public Map<String, Point> buildNodePositions(PlayerData playerData) {
        Map<String, Point> positions = new HashMap<>();
        positions.put(START_NODE_ID, new Point(0, 0));
        for (SkillCategory category : SkillCategory.values()) {
            List<Point> route = routeFor(category);
            positions.put(categoryNodeId(category), route.get(0));
            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
            for (int i = 0; i < skills.size(); i++) {
                int statRouteIndex = Math.min(route.size() - 1, i * 2 + 1);
                int skillRouteIndex = Math.min(route.size() - 1, i * 2 + 2);
                positions.put(statNodeId(category, i), route.get(statRouteIndex));
                positions.put(skillNodeId(skills.get(i)), route.get(skillRouteIndex));
            }
        }
        return positions;
    }

    public Map<String, List<String>> buildGraph(PlayerData playerData) {
        Map<String, List<String>> graph = new HashMap<>();
        connect(graph, START_NODE_ID, categoryNodeId(SkillCategory.ATTACK));
        connect(graph, START_NODE_ID, categoryNodeId(SkillCategory.DEFENSE));
        connect(graph, START_NODE_ID, categoryNodeId(SkillCategory.SUPPORT_MAGIC));

        for (SkillCategory category : SkillCategory.values()) {
            String categoryId = categoryNodeId(category);
            String previous = categoryId;
            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
            for (int i = 0; i < skills.size(); i++) {
                String stat = statNodeId(category, i);
                String skill = skillNodeId(skills.get(i));
                connect(graph, previous, stat);
                connect(graph, stat, skill);
                previous = skill;
            }
        }

        SkillCategory[] categories = SkillCategory.values();
        for (int i = 0; i < categories.length; i++) {
            SkillCategory current = categories[i];
            SkillCategory next = categories[(i + 1) % categories.length];
            connectCategoryNodes(graph, current, next);
            connectMatchingIndex(graph, playerData, current, next, 1);
            connectMatchingIndex(graph, playerData, current, next, 4);
        }
        connectMatchingIndex(graph, playerData, SkillCategory.ATTACK, SkillCategory.OFFENSIVE_MAGIC, 6);
        connectMatchingIndex(graph, playerData, SkillCategory.DEFENSE, SkillCategory.DEFENSIVE_MAGIC, 3);
        connectMatchingIndex(graph, playerData, SkillCategory.SUPPORT_MAGIC, SkillCategory.DEFENSIVE_MAGIC, 5);
        connectMatchingIndex(graph, playerData, SkillCategory.SUPPORT_MAGIC, SkillCategory.ATTACK, 2);
        return graph;
    }

    public boolean isAdjacentToCurrent(String nodeId, PlayerData playerData) {
        return buildGraph(playerData).getOrDefault(currentNodeId, new ArrayList<>()).contains(nodeId);
    }

    public String describeNode(PlayerData playerData, String nodeId) {
        if (START_NODE_ID.equals(nodeId)) {
            return "Start";
        }
        if (nodeId.startsWith("CAT:")) {
            return nodeId.substring(4).replace('_', ' ');
        }
        if (nodeId.startsWith("STAT:")) {
            NodeRef ref = parseProgressionNode(nodeId);
            return ref == null ? "Stat node" : statReward(ref.category, ref.index).getDescription();
        }
        if (nodeId.startsWith("SKILL:")) {
            Skill skill = playerData.getSkillTree().getSkill(nodeId.substring(6));
            return skill == null ? "Skill" : skill.getName();
        }
        return "Node";
    }

    public boolean isPurchasedOrUnlocked(PlayerData playerData, String nodeId) {
        if (START_NODE_ID.equals(nodeId) || nodeId.startsWith("CAT:")) {
            return true;
        }
        if (nodeId.startsWith("STAT:")) {
            NodeRef ref = parseProgressionNode(nodeId);
            return ref != null && isStatNodePurchased(ref.category, ref.index);
        }
        if (nodeId.startsWith("SKILL:")) {
            Skill skill = playerData.getSkillTree().getSkill(nodeId.substring(6));
            return skill != null && playerData.getSkillTree().isUnlocked(skill.getId());
        }
        return false;
    }

    public void reapplyPurchasedStats(PlayerData playerData) {
        playerData.resetTreeBonuses();
        for (String id : purchasedStatNodes) {
            NodeRef ref = parseProgressionNode(id);
            if (ref == null) {
                continue;
            }
            applyStatReward(playerData, ref.category, ref.index);
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
        return "STAT:" + category.name() + ":" + index;
    }

    private String categoryNodeId(SkillCategory category) {
        return "CAT:" + category.name();
    }

    private String skillNodeId(Skill skill) {
        return "SKILL:" + skill.getId();
    }

    private String legacyStatNodeId(SkillCategory category, int index) {
        return category.name() + ":" + index;
    }

    private NodeRef parseProgressionNode(String nodeId) {
        String raw = nodeId.startsWith("STAT:") ? nodeId.substring(5) : nodeId;
        String[] parts = raw.split(":");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new NodeRef(SkillCategory.valueOf(parts[0]), Integer.parseInt(parts[1]));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void connectMatchingIndex(Map<String, List<String>> graph, PlayerData playerData,
                                      SkillCategory first, SkillCategory second, int index) {
        List<Skill> firstSkills = playerData.getSkillTree().getSkillsByCategory(first);
        List<Skill> secondSkills = playerData.getSkillTree().getSkillsByCategory(second);
        if (index < firstSkills.size() && index < secondSkills.size()) {
            connect(graph, skillNodeId(firstSkills.get(index)), skillNodeId(secondSkills.get(index)));
        }
    }

    private void connectCategoryNodes(Map<String, List<String>> graph, SkillCategory first, SkillCategory second) {
        connect(graph, categoryNodeId(first), categoryNodeId(second));
    }

    private void connect(Map<String, List<String>> graph, String first, String second) {
        graph.computeIfAbsent(first, key -> new ArrayList<>()).add(second);
        graph.computeIfAbsent(second, key -> new ArrayList<>()).add(first);
    }

    private List<Point> routeFor(SkillCategory category) {
        int[][] route;
        switch (category) {
            case ATTACK:
                route = new int[][]{
                    {-42, -20}, {-92, -20}, {-132, -62}, {-98, -106}, {-42, -106},
                    {12, -136}, {74, -124}, {112, -76}, {86, -28}, {28, 10},
                    {-20, 48}, {-74, 42}, {-118, 2}, {-154, -46}, {-196, -48},
                    {-238, -8}, {-226, 48}, {-176, 76}, {-118, 72}, {-64, 96},
                    {-8, 82}
                };
                break;
            case DEFENSE:
                route = new int[][]{
                    {52, -6}, {106, -6}, {154, -38}, {208, -34}, {250, 8},
                    {238, 62}, {184, 92}, {128, 74}, {98, 28}, {142, -18},
                    {204, -76}, {270, -82}, {326, -42}, {340, 22}, {304, 76},
                    {244, 112}, {182, 132}, {124, 116}, {70, 136}
                };
                break;
            case OFFENSIVE_MAGIC:
                route = new int[][]{
                    {28, 76}, {76, 104}, {130, 102}, {174, 142}, {168, 202},
                    {112, 238}, {50, 224}, {8, 176}, {24, 120}, {86, 70},
                    {148, 34}, {210, 50}, {254, 100}, {252, 164}, {206, 218},
                    {138, 268}, {58, 286}, {-10, 254}, {-52, 204}, {-38, 150},
                    {18, 118}
                };
                break;
            case DEFENSIVE_MAGIC:
                route = new int[][]{
                    {-56, 76}, {-112, 102}, {-172, 90}, {-214, 136}, {-210, 198},
                    {-158, 236}, {-94, 222}, {-52, 174}, {-72, 120}, {-138, 72},
                    {-204, 38}, {-266, 56}, {-306, 112}, {-288, 174}, {-230, 220},
                    {-158, 266}, {-78, 286}, {-10, 248}
                };
                break;
            case SUPPORT_MAGIC:
                route = new int[][]{
                    {-52, -72}, {-112, -92}, {-162, -70}, {-206, -104}, {-198, -164},
                    {-142, -200}, {-76, -188}, {-32, -140}, {-54, -82}, {-120, -34},
                    {-188, -8}, {-250, -34}, {-292, -88}, {-280, -150}, {-222, -202},
                    {-150, -236}, {-68, -232}, {-2, -196}
                };
                break;
            default:
                route = new int[][]{{0, 0}};
                break;
        }
        List<Point> points = new ArrayList<>();
        for (int[] coordinate : route) {
            points.add(new Point(coordinate[0], coordinate[1]));
        }
        return points;
    }

    private static class NodeRef {
        private final SkillCategory category;
        private final int index;

        private NodeRef(SkillCategory category, int index) {
            this.category = category;
            this.index = index;
        }
    }
}
