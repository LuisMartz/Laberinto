package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeMenuRenderer {
    private static final Color PANEL = new Color(9, 10, 13, 235);
    private static final Color EDGE = new Color(202, 178, 104);
    private static final Color LINE_LOCKED = new Color(82, 78, 72, 170);
    private static final Color LINE_OPEN = new Color(184, 152, 75, 210);
    private static final Color TEXT = new Color(235, 229, 205);
    private static final Color MUTED = new Color(158, 155, 143);
    private static final Color LOCKED = new Color(44, 45, 51);
    private static final Color AVAILABLE = new Color(192, 158, 61);
    private static final Color UNLOCKED = new Color(72, 162, 101);
    private static final Color STAT = new Color(67, 103, 154);
    private static final Color SELECT = new Color(205, 76, 47);

    public void draw(Graphics2D g, PlayerData playerData, MazeState mazeState, SkillTreeProgression progression,
                     int x, int y, int width, int height, int selectedCategoryIndex, int selectedSkillIndex,
                     String message) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawPanel(g2, x, y, width, height);
        drawHeader(g2, playerData, mazeState, x, y, width);

        int centerX = x + width / 2;
        int centerY = y + height / 2 + 14;
        int maxRadius = Math.min(width, height) / 2 - 70;

        Map<String, Point> skillPositions = new HashMap<>();
        Map<SkillCategory, Point> branchAnchors = new EnumMap<>(SkillCategory.class);

        drawStartNode(g2, centerX, centerY);
        layoutBranches(g2, playerData, progression, centerX, centerY, maxRadius, selectedCategoryIndex, selectedSkillIndex, skillPositions, branchAnchors);
        drawPrerequisiteLines(g2, playerData, skillPositions);
        drawLegend(g2, x + 18, y + height - 42);
        drawSelectedInfo(g2, playerData, progression, x + width - 270, y + height - 72, selectedCategoryIndex, selectedSkillIndex, message);

        g2.dispose();
    }

    private void layoutBranches(Graphics2D g2, PlayerData playerData, SkillTreeProgression progression, int centerX, int centerY, int maxRadius,
                                int selectedCategoryIndex, int selectedSkillIndex,
                                Map<String, Point> skillPositions, Map<SkillCategory, Point> branchAnchors) {
        SkillCategory[] categories = SkillCategory.values();
        double startAngle = -Math.PI / 2;
        double angleStep = Math.PI * 2 / categories.length;

        for (int c = 0; c < categories.length; c++) {
            SkillCategory category = categories[c];
            double angle = startAngle + c * angleStep;
            int anchorX = centerX + (int) Math.round(Math.cos(angle) * 66);
            int anchorY = centerY + (int) Math.round(Math.sin(angle) * 66);
            branchAnchors.put(category, new Point(anchorX, anchorY));

            g2.setColor(c == selectedCategoryIndex ? LINE_OPEN : LINE_LOCKED);
            g2.setStroke(new BasicStroke(c == selectedCategoryIndex ? 3 : 2));
            g2.drawLine(centerX, centerY, anchorX, anchorY);
            drawCategoryNode(g2, category, anchorX, anchorY, c == selectedCategoryIndex);

            List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(category);
            int count = Math.max(1, skills.size());
            int usableRadius = Math.max(110, maxRadius);

            Point previous = new Point(anchorX, anchorY);
            for (int i = 0; i < skills.size(); i++) {
                int radius = 112 + (i * usableRadius / count);
                int skillX = centerX + (int) Math.round(Math.cos(angle) * radius);
                int skillY = centerY + (int) Math.round(Math.sin(angle) * radius);
                Point skillPoint = new Point(skillX, skillY);
                Skill skill = skills.get(i);

                Point statPoint = midpoint(previous, skillPoint);
                drawProgressLine(g2, previous, statPoint, c == selectedCategoryIndex);
                boolean statPurchased = progression.isStatNodePurchased(category, i);
                boolean statAvailable = progression.canPurchaseStatNode(playerData, category, i);
                drawStatNode(g2, statPoint.x, statPoint.y, progression.statReward(category, i).getLabel(), statPurchased, statAvailable);
                drawProgressLine(g2, statPoint, skillPoint, c == selectedCategoryIndex);

                boolean selected = c == selectedCategoryIndex && i == selectedSkillIndex;
                drawSkillNode(g2, playerData, progression, skill, i, skillX, skillY, selected);
                skillPositions.put(skill.getId(), skillPoint);
                previous = skillPoint;
            }
        }
        g2.setStroke(new BasicStroke(1));
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
                if (from == null) {
                    continue;
                }
                g2.setColor(playerData.getSkillTree().isUnlocked(prerequisite) ? LINE_OPEN : LINE_LOCKED);
                g2.drawLine(from.x, from.y, to.x, to.y);
            }
        }
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
        g2.drawString("SKILL CONSTELLATION", x + 18, y + 28);
        g2.setFont(new Font("Dialog", Font.PLAIN, 12));
        g2.setColor(MUTED);
        g2.drawString("A/D category   Up/Down node   Enter buy path / unlock skill   M/Esc close", x + 18, y + 47);
        g2.setColor(TEXT);
        g2.drawString("Coins: " + mazeState.getScore() + "   Skill points: " + playerData.getSkillPoints(), x + width - 245, y + 28);
    }

    private void drawStartNode(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(24, 24, 30));
        g2.fillOval(x - 28, y - 28, 56, 56);
        g2.setColor(EDGE);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(x - 28, y - 28, 56, 56);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(TEXT);
        drawCentered(g2, "START", x, y + 5);
    }

    private void drawCategoryNode(Graphics2D g2, SkillCategory category, int x, int y, boolean selected) {
        g2.setColor(selected ? new Color(75, 42, 35) : new Color(28, 28, 35));
        g2.fillOval(x - 20, y - 20, 40, 40);
        g2.setColor(selected ? SELECT : EDGE);
        g2.setStroke(new BasicStroke(selected ? 3 : 2));
        g2.drawOval(x - 20, y - 20, 40, 40);
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Dialog", Font.BOLD, 9));
        g2.setColor(TEXT);
        drawCentered(g2, shortCategory(category), x, y + 4);
    }

    private void drawStatNode(Graphics2D g2, int x, int y, String label, boolean purchased, boolean available) {
        g2.setColor(purchased ? UNLOCKED : available ? STAT : LOCKED);
        g2.fillRect(x - 10, y - 10, 20, 20);
        g2.setColor(purchased ? TEXT : new Color(190, 215, 250));
        g2.drawRect(x - 10, y - 10, 20, 20);
        g2.setFont(new Font("Dialog", Font.BOLD, 8));
        g2.setColor(Color.WHITE);
        drawCentered(g2, label, x, y + 3);
    }

    private void drawSkillNode(Graphics2D g2, PlayerData playerData, SkillTreeProgression progression, Skill skill, int index, int x, int y, boolean selected) {
        boolean unlocked = playerData.getSkillTree().isUnlocked(skill.getId());
        boolean canUnlock = progression.canUnlockSkill(playerData, skill, index);
        Color fill = unlocked ? UNLOCKED : canUnlock ? AVAILABLE : LOCKED;
        int radius = skill.getType() == SkillType.PASSIVE ? 13 : 15;

        g2.setColor(fill);
        g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setColor(selected ? SELECT : TEXT);
        g2.setStroke(new BasicStroke(selected ? 3 : 2));
        g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(new Font("Dialog", Font.BOLD, 8));
        g2.setColor(Color.BLACK);
        drawCentered(g2, skill.getType() == SkillType.ACTIVE ? "A" : "P", x, y + 3);
    }

    private void drawProgressLine(Graphics2D g2, Point from, Point to, boolean activeBranch) {
        g2.setColor(activeBranch ? LINE_OPEN : LINE_LOCKED);
        g2.setStroke(new BasicStroke(activeBranch ? 2 : 1));
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
        Skill selected = skills.get(Math.max(0, Math.min(selectedSkillIndex, skills.size() - 1)));
        int index = Math.max(0, Math.min(selectedSkillIndex, skills.size() - 1));
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

    private Point midpoint(Point a, Point b) {
        return new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
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
}
