package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.HashMap;
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
    private static final int VIRTUAL_CENTER_X = 0;
    private static final int VIRTUAL_CENTER_Y = 0;

    public void draw(Graphics2D g, PlayerData playerData, MazeState mazeState, SkillTreeProgression progression,
                     int x, int y, int width, int height, int selectedCategoryIndex, int selectedSkillIndex,
                     String message) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawPanel(g2, x, y, width, height);
        drawHeader(g2, playerData, mazeState, x, y, width);

        Rectangle viewport = new Rectangle(x + 14, y + 58, width - 28, height - 126);
        SkillMapLayout layout = buildLayout(playerData);
        Point selected = layout.getSelectedPoint(selectedCategoryIndex, selectedSkillIndex);
        int cameraX = viewport.x + viewport.width / 2 - selected.x;
        int cameraY = viewport.y + viewport.height / 2 - selected.y;

        Graphics2D map = (Graphics2D) g2.create();
        map.setClip(viewport);
        drawViewportBackground(map, viewport);
        map.translate(cameraX, cameraY);
        drawMap(map, playerData, progression, layout, selectedCategoryIndex, selectedSkillIndex);
        map.dispose();

        g2.setColor(EDGE);
        g2.drawRect(viewport.x, viewport.y, viewport.width, viewport.height);
        drawLegend(g2, x + 18, y + height - 42);
        drawSelectedInfo(g2, playerData, progression, x + width - 290, y + height - 78,
                selectedCategoryIndex, selectedSkillIndex, message);
        g2.dispose();
    }

    private SkillMapLayout buildLayout(PlayerData playerData) {
        SkillMapLayout layout = new SkillMapLayout();
        layout.start = new Point(VIRTUAL_CENTER_X, VIRTUAL_CENTER_Y);

        for (SkillCategory category : SkillCategory.values()) {
            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
            BranchShape shape = branchShape(category);
            Point previousSkill = new Point(shape.anchorX, shape.anchorY);
            layout.categoryAnchors.put(category, previousSkill);

            for (int i = 0; i < skills.size(); i++) {
                Point skillPoint = pointOnBranch(shape, i);
                Point statPoint = new Point((previousSkill.x + skillPoint.x) / 2, (previousSkill.y + skillPoint.y) / 2);
                Skill skill = skills.get(i);
                layout.skillPositions.put(skill.getId(), skillPoint);
                layout.selectedPositions.put(nodeKey(category, i), skillPoint);
                layout.statPositions.put(nodeKey(category, i), statPoint);
                previousSkill = skillPoint;
            }
        }
        return layout;
    }

    private void drawMap(Graphics2D g2, PlayerData playerData, SkillTreeProgression progression, SkillMapLayout layout,
                         int selectedCategoryIndex, int selectedSkillIndex) {
        drawStartNode(g2, layout.start.x, layout.start.y);

        for (SkillCategory category : SkillCategory.values()) {
            Point anchor = layout.categoryAnchors.get(category);
            boolean selectedBranch = category.ordinal() == selectedCategoryIndex;
            drawProgressLine(g2, layout.start, anchor, selectedBranch);
            drawCategoryNode(g2, category, anchor.x, anchor.y, selectedBranch);

            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
            Point previous = anchor;
            for (int i = 0; i < skills.size(); i++) {
                Skill skill = skills.get(i);
                Point stat = layout.statPositions.get(nodeKey(category, i));
                Point point = layout.skillPositions.get(skill.getId());
                boolean activeLine = selectedBranch || progression.isStatNodePurchased(category, i);
                drawProgressLine(g2, previous, stat, activeLine);
                drawProgressLine(g2, stat, point, activeLine);
                drawStatNode(g2, stat.x, stat.y, progression.statReward(category, i).getLabel(),
                        progression.isStatNodePurchased(category, i),
                        progression.canPurchaseStatNode(playerData, category, i));
                drawSkillNode(g2, playerData, progression, skill, i, point.x, point.y,
                        category.ordinal() == selectedCategoryIndex && i == selectedSkillIndex);
                previous = point;
            }
        }

        drawPrerequisiteLines(g2, playerData, layout.skillPositions);
    }

    private Point pointOnBranch(BranchShape shape, int index) {
        int step = index + 1;
        int wave = ((index % 4) - 1) * 24;
        int curl = (index / 3) * shape.curl;
        int x = shape.anchorX + shape.dx * step + shape.px * wave + shape.px * curl;
        int y = shape.anchorY + shape.dy * step + shape.py * wave + shape.py * curl;
        return new Point(x, y);
    }

    private BranchShape branchShape(SkillCategory category) {
        switch (category) {
            case ATTACK:
                return new BranchShape(0, -86, 0, -72, 1, 0, 18);
            case DEFENSE:
                return new BranchShape(92, -24, 76, -8, 0, 1, 20);
            case OFFENSIVE_MAGIC:
                return new BranchShape(72, 76, 58, 54, -1, 1, -18);
            case DEFENSIVE_MAGIC:
                return new BranchShape(-72, 76, -58, 54, -1, -1, 18);
            case SUPPORT_MAGIC:
                return new BranchShape(-92, -24, -76, -8, 0, -1, -20);
            default:
                return new BranchShape(0, 0, 64, 0, 0, 1, 0);
        }
    }

    private void drawPrerequisiteLines(Graphics2D g2, PlayerData playerData, Map<String, Point> positions) {
        g2.setStroke(new BasicStroke(1));
        for (Skill skill : playerData.getSkillTree().getAllSkills()) {
            Point to = positions.get(skill.getId());
            if (to == null) {
                continue;
            }
            for (String prerequisite : skill.getPrerequisites()) {
                Point from = positions.get(prerequisite);
                if (from != null) {
                    g2.setColor(playerData.getSkillTree().isUnlocked(prerequisite) ? LINE_OPEN : LINE_LOCKED);
                    g2.drawLine(from.x, from.y, to.x, to.y);
                }
            }
        }
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
        g2.drawString("A/D branch   Up/Down node   Enter buy/unlock   M/Esc close", x + 18, y + 47);
        g2.setColor(TEXT);
        g2.drawString("Coins: " + mazeState.getScore() + "   Skill points: " + playerData.getSkillPoints(), x + width - 245, y + 28);
    }

    private void drawStartNode(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(24, 24, 30));
        g2.fillOval(x - 30, y - 30, 60, 60);
        g2.setColor(EDGE);
        g2.setStroke(new BasicStroke(4));
        g2.drawOval(x - 30, y - 30, 60, 60);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(TEXT);
        drawCentered(g2, "START", x, y + 5);
    }

    private void drawCategoryNode(Graphics2D g2, SkillCategory category, int x, int y, boolean selected) {
        g2.setColor(selected ? new Color(75, 42, 35) : new Color(28, 28, 35));
        g2.fillOval(x - 23, y - 23, 46, 46);
        g2.setColor(selected ? SELECT : EDGE);
        g2.setStroke(new BasicStroke(selected ? 4 : 2));
        g2.drawOval(x - 23, y - 23, 46, 46);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Dialog", Font.BOLD, 10));
        g2.setColor(TEXT);
        drawCentered(g2, shortCategory(category), x, y + 4);
    }

    private void drawStatNode(Graphics2D g2, int x, int y, String label, boolean purchased, boolean available) {
        int radius = 13;
        g2.setColor(purchased ? UNLOCKED : available ? STAT : LOCKED);
        g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setColor(purchased ? TEXT : new Color(190, 215, 250));
        g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setFont(new Font("Dialog", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        drawCentered(g2, label, x, y + 3);
    }

    private void drawSkillNode(Graphics2D g2, PlayerData playerData, SkillTreeProgression progression, Skill skill, int index, int x, int y, boolean selected) {
        boolean unlocked = playerData.getSkillTree().isUnlocked(skill.getId());
        boolean canUnlock = progression.canUnlockSkill(playerData, skill, index);
        Color fill = unlocked ? UNLOCKED : canUnlock ? AVAILABLE : LOCKED;
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
                                  int selectedCategoryIndex, int selectedSkillIndex, String message) {
        SkillCategory category = SkillCategory.values()[Math.max(0, Math.min(selectedCategoryIndex, SkillCategory.values().length - 1))];
        List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
        g2.setFont(new Font("Dialog", Font.BOLD, 12));
        g2.setColor(TEXT);
        if (skills.isEmpty()) {
            g2.drawString("No skills", x, y);
            return;
        }
        int index = Math.max(0, Math.min(selectedSkillIndex, skills.size() - 1));
        Skill selected = skills.get(index);
        boolean statPurchased = progression.isStatNodePurchased(category, index);
        g2.drawString(selected.getName(), x, y);
        g2.setFont(new Font("Dialog", Font.PLAIN, 11));
        g2.setColor(MUTED);
        g2.drawString(selected.getShortDescription(), x, y + 16);
        if (!statPurchased) {
            g2.drawString("Buy path: " + progression.statReward(category, index).getDescription()
                    + " / " + progression.statNodeCost(category, index) + " coins", x, y + 32);
        } else {
            g2.drawString("Unlock: " + progression.skillNodeCost(selected, index) + " coins + 1 skill point", x, y + 32);
        }
        if (message != null && !message.isEmpty()) {
            g2.setColor(TEXT);
            g2.drawString(message, x, y + 50);
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

    private String nodeKey(SkillCategory category, int index) {
        return category.name() + ":" + index;
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int baselineY) {
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private static class BranchShape {
        private final int anchorX;
        private final int anchorY;
        private final int dx;
        private final int dy;
        private final int px;
        private final int py;
        private final int curl;

        private BranchShape(int anchorX, int anchorY, int dx, int dy, int px, int py, int curl) {
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.dx = dx;
            this.dy = dy;
            this.px = px;
            this.py = py;
            this.curl = curl;
        }
    }

    private static class SkillMapLayout {
        private Point start;
        private final Map<SkillCategory, Point> categoryAnchors = new EnumMap<>(SkillCategory.class);
        private final Map<String, Point> skillPositions = new HashMap<>();
        private final Map<String, Point> statPositions = new HashMap<>();

        private Point getSelectedPoint(int categoryIndex, int skillIndex) {
            SkillCategory[] categories = SkillCategory.values();
            SkillCategory category = categories[Math.max(0, Math.min(categoryIndex, categories.length - 1))];
            Point anchor = categoryAnchors.get(category);
            Point selected = selectedPositions.get(category.name() + ":" + skillIndex);
            return selected == null ? anchor == null ? start : anchor : selected;
        }

        private final Map<String, Point> selectedPositions = new HashMap<>();
    }
}
