package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;

public class SkillTreeMenuRenderer {
    private static final Color PANEL = new Color(9, 10, 13, 238);
    private static final Color EDGE = new Color(202, 178, 104);
    private static final Color LINE_LOCKED = new Color(65, 69, 78, 190);
    private static final Color LINE_OPEN = new Color(50, 131, 183, 220);
    private static final Color TEXT = new Color(235, 229, 205);
    private static final Color MUTED = new Color(158, 155, 143);
    private static final Color LOCKED = new Color(35, 39, 48);
    private static final Color AVAILABLE = new Color(201, 164, 54);
    private static final Color UNLOCKED = new Color(77, 170, 105);
    private static final Color STAT = new Color(69, 116, 179);
    private static final Color SELECT = new Color(220, 84, 50);

    public void draw(Graphics2D g, PlayerData playerData, MazeState mazeState, SkillTreeProgression progression,
                     int x, int y, int width, int height, String selectedNodeId, String message) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawPanel(g2, x, y, width, height);
        drawHeader(g2, playerData, mazeState, x, y, width);

        Rectangle viewport = new Rectangle(x + 14, y + 58, width - 28, height - 126);
        Map<String, Point> positions = progression.buildNodePositions(playerData);
        Point selected = positions.getOrDefault(selectedNodeId, positions.get(SkillTreeProgression.START_NODE_ID));
        int cameraX = viewport.x + viewport.width / 2 - selected.x;
        int cameraY = viewport.y + viewport.height / 2 - selected.y;

        Graphics2D map = (Graphics2D) g2.create();
        map.setClip(viewport);
        drawViewportBackground(map, viewport);
        map.translate(cameraX, cameraY);
        drawMap(map, playerData, progression, positions, selectedNodeId);
        map.dispose();

        g2.setColor(EDGE);
        g2.drawRect(viewport.x, viewport.y, viewport.width, viewport.height);
        drawLegend(g2, x + 18, y + height - 42);
        drawSelectedInfo(g2, playerData, progression, x + width - 290, y + height - 78,
                selectedNodeId, message);
        g2.dispose();
    }

    private void drawMap(Graphics2D g2, PlayerData playerData, SkillTreeProgression progression,
                         Map<String, Point> positions, String selectedNodeId) {
        Map<String, List<String>> graph = progression.buildGraph(playerData);
        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            Point from = positions.get(entry.getKey());
            if (from == null) {
                continue;
            }
            for (String target : entry.getValue()) {
                if (entry.getKey().compareTo(target) > 0) {
                    continue;
                }
                Point to = positions.get(target);
                if (to != null) {
                    boolean active = progression.isPurchasedOrUnlocked(playerData, entry.getKey())
                            && progression.isPurchasedOrUnlocked(playerData, target);
                    drawProgressLine(g2, from, to, active);
                }
            }
        }

        for (SkillCategory category : SkillCategory.values()) {
            Point anchor = positions.get(categoryNodeId(category));
            drawCategoryNode(g2, category, anchor.x, anchor.y, selectedNodeId.equals(categoryNodeId(category)),
                    progression.getCurrentNodeId().equals(categoryNodeId(category)));

            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
            for (int i = 0; i < skills.size(); i++) {
                Skill skill = skills.get(i);
                String statId = statNodeId(category, i);
                String skillId = skillNodeId(skill);
                Point stat = positions.get(statId);
                Point point = positions.get(skillId);
                drawStatNode(g2, stat.x, stat.y, progression.statReward(category, i).getLabel(),
                        progression.isStatNodePurchased(category, i),
                        progression.isAdjacentToCurrent(statId, playerData),
                        selectedNodeId.equals(statId), progression.getCurrentNodeId().equals(statId));
                drawSkillNode(g2, playerData, progression, skill, i, point.x, point.y,
                        selectedNodeId.equals(skillId), progression.getCurrentNodeId().equals(skillId));
            }
        }

        Point start = positions.get(SkillTreeProgression.START_NODE_ID);
        drawStartNode(g2, start.x, start.y, selectedNodeId.equals(SkillTreeProgression.START_NODE_ID),
                progression.getCurrentNodeId().equals(SkillTreeProgression.START_NODE_ID));
    }

    private void drawViewportBackground(Graphics2D g2, Rectangle viewport) {
        g2.setColor(new Color(5, 7, 10));
        g2.fillRect(viewport.x, viewport.y, viewport.width, viewport.height);
        g2.setColor(new Color(29, 44, 58, 90));
        for (int i = -viewport.width; i < viewport.width * 2; i += 48) {
            g2.drawLine(viewport.x + i, viewport.y, viewport.x + i + viewport.height, viewport.y + viewport.height);
        }
        g2.setColor(new Color(65, 97, 117, 45));
        g2.drawOval(viewport.x + viewport.width / 2 - 150, viewport.y + viewport.height / 2 - 150, 300, 300);
        g2.drawOval(viewport.x + viewport.width / 2 - 230, viewport.y + viewport.height / 2 - 230, 460, 460);
    }

    private void drawPanel(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(PANEL);
        g2.fillRoundRect(x, y, width, height, 8, 8);
        g2.setColor(EDGE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, width, height, 8, 8);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawHeader(Graphics2D g2, PlayerData playerData, MazeState mazeState, int x, int y, int width) {
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.setColor(TEXT);
        g2.drawString("SPHERE GRID", x + 18, y + 28);
        g2.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2.setColor(MUTED);
        g2.drawString("Arrows follow connected roads   Enter move/buy adjacent node", x + 18, y + 47);
        g2.setColor(TEXT);
        g2.drawString("Coins: " + mazeState.getScore() + "   Skill points: " + playerData.getSkillPoints(), x + width - 245, y + 28);
    }

    private void drawStartNode(Graphics2D g2, int x, int y, boolean selected, boolean current) {
        g2.setColor(current ? new Color(55, 94, 78) : new Color(24, 24, 30));
        g2.fillOval(x - 30, y - 30, 60, 60);
        g2.setColor(selected ? SELECT : EDGE);
        g2.setStroke(new BasicStroke(selected ? 5 : 4));
        g2.drawOval(x - 30, y - 30, 60, 60);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(TEXT);
        drawCentered(g2, "START", x, y + 5);
    }

    private void drawCategoryNode(Graphics2D g2, SkillCategory category, int x, int y, boolean selected, boolean current) {
        g2.setColor(current ? new Color(55, 94, 78) : selected ? new Color(75, 42, 35) : new Color(28, 28, 35));
        g2.fillOval(x - 23, y - 23, 46, 46);
        g2.setColor(selected ? SELECT : EDGE);
        g2.setStroke(new BasicStroke(selected ? 4 : 2));
        g2.drawOval(x - 23, y - 23, 46, 46);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Dialog", Font.BOLD, 10));
        g2.setColor(TEXT);
        drawCentered(g2, shortCategory(category), x, y + 4);
    }

    private void drawStatNode(Graphics2D g2, int x, int y, String label, boolean purchased, boolean available,
                              boolean selected, boolean current) {
        int radius = 13;
        g2.setColor(current ? new Color(91, 181, 141) : purchased ? UNLOCKED : available ? STAT : LOCKED);
        g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setColor(selected ? SELECT : purchased ? TEXT : new Color(190, 215, 250));
        g2.setStroke(new BasicStroke(selected ? 4 : 1));
        g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Dialog", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        drawCentered(g2, label, x, y + 3);
    }

    private void drawSkillNode(Graphics2D g2, PlayerData playerData, SkillTreeProgression progression, Skill skill,
                               int index, int x, int y, boolean selected, boolean current) {
        boolean unlocked = playerData.getSkillTree().isUnlocked(skill.getId());
        boolean canUnlock = progression.canUnlockSkill(playerData, skill, index);
        Color fill = current ? new Color(91, 181, 141) : unlocked ? UNLOCKED : canUnlock ? AVAILABLE : LOCKED;
        int radius = skill.getType() == SkillType.PASSIVE ? 16 : 18;

        g2.setColor(fill);
        g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setColor(selected ? SELECT : TEXT);
        g2.setStroke(new BasicStroke(selected ? 4 : 2));
        g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Dialog", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        drawCentered(g2, skill.getType() == SkillType.ACTIVE ? "A" : "P", x, y + 4);

        if (selected) {
            g2.setFont(new Font("Dialog", Font.BOLD, 12));
            g2.setColor(TEXT);
            g2.drawString(skill.getName(), x + radius + 8, y - 4);
        }
    }

    private void drawProgressLine(Graphics2D g2, Point from, Point to, boolean activeBranch) {
        g2.setColor(activeBranch ? LINE_OPEN : LINE_LOCKED);
        g2.setStroke(new BasicStroke(activeBranch ? 4 : 2));
        g2.drawLine(from.x, from.y, to.x, to.y);
    }

    private void drawLegend(Graphics2D g2, int x, int y) {
        g2.setFont(new Font("Dialog", Font.PLAIN, 11));
        drawLegendItem(g2, x, y, UNLOCKED, "Unlocked");
        drawLegendItem(g2, x + 92, y, AVAILABLE, "Available");
        drawLegendItem(g2, x + 190, y, LOCKED, "Locked");
        drawLegendItem(g2, x + 278, y, STAT, "Stat path");
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillOval(x, y - 9, 12, 12);
        g2.setColor(TEXT);
        g2.drawString(label, x + 18, y);
    }

    private void drawSelectedInfo(Graphics2D g2, PlayerData playerData, SkillTreeProgression progression, int x, int y,
                                  String selectedNodeId, String message) {
        g2.setFont(new Font("Dialog", Font.BOLD, 12));
        g2.setColor(TEXT);
        g2.drawString(progression.describeNode(playerData, selectedNodeId), x, y);
        g2.setFont(new Font("Dialog", Font.PLAIN, 11));
        g2.setColor(MUTED);
        g2.drawString("Current: " + progression.describeNode(playerData, progression.getCurrentNodeId()), x, y + 16);
        if (!progression.isAdjacentToCurrent(selectedNodeId, playerData) && !selectedNodeId.equals(progression.getCurrentNodeId())) {
            g2.drawString("Move through connected adjacent nodes.", x, y + 32);
        } else if (selectedNodeId.startsWith("STAT:")) {
            NodeRef ref = parseStatNode(selectedNodeId);
            if (ref != null && !progression.isStatNodePurchased(ref.category, ref.index)) {
                g2.drawString("Buy " + progression.statReward(ref.category, ref.index).getDescription()
                        + " / " + progression.statNodeCost(ref.category, ref.index) + " coins", x, y + 32);
            } else {
                g2.drawString("Travel: " + progression.travelCost() + " coins", x, y + 32);
            }
        } else if (selectedNodeId.startsWith("SKILL:")) {
            Skill skill = playerData.getSkillTree().getSkill(selectedNodeId.substring(6));
            if (skill != null) {
                List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(skill.getCategory());
                int index = skills.indexOf(skill);
                g2.drawString(skill.getShortDescription(), x, y + 32);
                g2.drawString(playerData.getSkillTree().isUnlocked(skill.getId())
                        ? "Travel: " + progression.travelCost() + " coins"
                        : "Unlock: " + progression.skillNodeCost(skill, index) + " coins + 1 skill point", x, y + 48);
            }
        } else {
            g2.drawString("Travel: " + progression.travelCost() + " coins", x, y + 32);
        }
        if (message != null && !message.isEmpty()) {
            g2.setColor(TEXT);
            g2.drawString(message, x, y + 64);
        }
    }

    private String shortCategory(SkillCategory category) {
        switch (category) {
            case ATTACK:
                return "ATK";
            case DEFENSE:
                return "DEF";
            case OFFENSIVE_MAGIC:
                return "MAG+";
            case DEFENSIVE_MAGIC:
                return "MAG-";
            case SUPPORT_MAGIC:
                return "SUP";
            default:
                return "SKL";
        }
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int baselineY) {
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private String categoryNodeId(SkillCategory category) {
        return "CAT:" + category.name();
    }

    private String statNodeId(SkillCategory category, int index) {
        return "STAT:" + category.name() + ":" + index;
    }

    private String skillNodeId(Skill skill) {
        return "SKILL:" + skill.getId();
    }

    private NodeRef parseStatNode(String nodeId) {
        String[] parts = nodeId.substring(5).split(":");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new NodeRef(SkillCategory.valueOf(parts[0]), Integer.parseInt(parts[1]));
        } catch (IllegalArgumentException ex) {
            return null;
        }
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
