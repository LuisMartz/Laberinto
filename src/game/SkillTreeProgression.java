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
    private static final int GRID_CELL_SIZE = 82;
    private static final int ROOM_SIZE = 5;
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
            List<GridCell> route = generatedRoomRoute(category);
            Point origin = roomOrigin(category);
            positions.put(categoryNodeId(category), toPoint(origin, route.get(0)));
            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
            for (int i = 0; i < skills.size(); i++) {
                positions.put(statNodeId(category, i), toPoint(origin, route.get(i * 2 + 1)));
                positions.put(skillNodeId(skills.get(i)), toPoint(origin, route.get(i * 2 + 2)));
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
            connectCategoryRoute(graph, playerData, category);
        }

        connectBridge(graph, playerData, SkillCategory.ATTACK, 4, SkillCategory.OFFENSIVE_MAGIC);
        connectBridge(graph, playerData, SkillCategory.DEFENSE, 3, SkillCategory.DEFENSIVE_MAGIC);
        connectBridge(graph, playerData, SkillCategory.SUPPORT_MAGIC, 5, SkillCategory.DEFENSIVE_MAGIC);
        connectBridge(graph, playerData, SkillCategory.OFFENSIVE_MAGIC, 6, SkillCategory.DEFENSIVE_MAGIC);
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

    private void connectCategoryRoute(Map<String, List<String>> graph, PlayerData playerData, SkillCategory category) {
        List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
        String previous = categoryNodeId(category);
        for (int i = 0; i < skills.size(); i++) {
            String stat = statNodeId(category, i);
            String skill = skillNodeId(skills.get(i));
            connect(graph, previous, stat);
            connect(graph, stat, skill);
            previous = skill;
        }
    }

    private void connectBridge(Map<String, List<String>> graph, PlayerData playerData,
                               SkillCategory fromCategory, int fromSkillIndex, SkillCategory toCategory) {
        List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(fromCategory);
        if (fromSkillIndex < skills.size()) {
            connect(graph, skillNodeId(skills.get(fromSkillIndex)), categoryNodeId(toCategory));
        }
    }

    private void connect(Map<String, List<String>> graph, String first, String second) {
        graph.computeIfAbsent(first, key -> new ArrayList<>()).add(second);
        graph.computeIfAbsent(second, key -> new ArrayList<>()).add(first);
    }

    private Point roomOrigin(SkillCategory category) {
        switch (category) {
            case ATTACK:
                return new Point(-3, -6);
            case DEFENSE:
                return new Point(2, -5);
            case OFFENSIVE_MAGIC:
                return new Point(1, 1);
            case DEFENSIVE_MAGIC:
                return new Point(-4, 1);
            case SUPPORT_MAGIC:
                return new Point(-5, -2);
            default:
                return new Point(0, 0);
        }
    }

    private Point toPoint(Point roomOrigin, GridCell cell) {
        return new Point((roomOrigin.x + cell.column) * GRID_CELL_SIZE,
                (roomOrigin.y + cell.row) * GRID_CELL_SIZE);
    }

    private List<GridCell> generatedRoomRoute(SkillCategory category) {
        switch (category) {
            case ATTACK:
                return snakeRoute(true, true);
            case DEFENSE:
                return snakeRoute(true, false);
            case OFFENSIVE_MAGIC:
                return snakeRoute(false, false);
            case DEFENSIVE_MAGIC:
                return snakeRoute(false, true);
            case SUPPORT_MAGIC:
                return verticalSnakeRoute(false, true);
            default:
                return snakeRoute(false, false);
        }
    }

    private List<GridCell> snakeRoute(boolean startAtBottom, boolean startAtRight) {
        List<GridCell> route = new ArrayList<>();
        for (int rowStep = 0; rowStep < ROOM_SIZE; rowStep++) {
            int row = startAtBottom ? ROOM_SIZE - 1 - rowStep : rowStep;
            boolean reverse = (rowStep % 2 == 0) == startAtRight;
            for (int columnStep = 0; columnStep < ROOM_SIZE; columnStep++) {
                int column = reverse ? ROOM_SIZE - 1 - columnStep : columnStep;
                route.add(new GridCell(row, column));
            }
        }
        return route;
    }

    private List<GridCell> verticalSnakeRoute(boolean startAtBottom, boolean startAtRight) {
        List<GridCell> route = new ArrayList<>();
        for (int columnStep = 0; columnStep < ROOM_SIZE; columnStep++) {
            int column = startAtRight ? ROOM_SIZE - 1 - columnStep : columnStep;
            boolean reverse = (columnStep % 2 == 0) == startAtBottom;
            for (int rowStep = 0; rowStep < ROOM_SIZE; rowStep++) {
                int row = reverse ? ROOM_SIZE - 1 - rowStep : rowStep;
                route.add(new GridCell(row, column));
            }
        }
        return route;
    }

    private static class NodeRef {
        private final SkillCategory category;
        private final int index;

        private NodeRef(SkillCategory category, int index) {
            this.category = category;
            this.index = index;
        }
    }

    private static class GridCell {
        private final int row;
        private final int column;

        private GridCell(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }
}
