package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

class GameMenuRenderer {
    static final int BASE_WIDTH = 800;
    static final int BASE_HEIGHT = 600;
    static final int CONTENT_HEIGHT = 400;

    private static final Color BACKDROP = new Color(0, 0, 0, 175);
    private static final Color PANEL = new Color(18, 18, 24, 238);
    private static final Color PANEL_EDGE = new Color(230, 220, 165);
    private static final Color TAB = new Color(35, 35, 48);
    private static final Color TAB_ACTIVE = new Color(88, 56, 44);
    private static final Color SELECTION = new Color(118, 56, 42);
    private static final Color MUTED_TEXT = new Color(170, 170, 180);
    private static final String[] TABS = {"Stats", "Inventory", "Skills", "Floor"};

    private final InventoryMenuRenderer inventoryMenuRenderer;
    private final SkillTreeMenuRenderer skillTreeMenuRenderer;

    GameMenuRenderer(InventoryMenuRenderer inventoryMenuRenderer, SkillTreeMenuRenderer skillTreeMenuRenderer) {
        this.inventoryMenuRenderer = inventoryMenuRenderer;
        this.skillTreeMenuRenderer = skillTreeMenuRenderer;
    }

    void draw(Graphics g, PlayerData playerData, MazeState mazeState, SkillTreeProgression progression,
              GameMenuController menuController, String message) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(BACKDROP);
        g2.fillRect(0, 0, BASE_WIDTH, BASE_HEIGHT);

        Rectangle bounds = menuBounds(menuController.getTabIndex());
        drawFrame(g2, bounds, menuController.getTabIndex());

        int contentX = bounds.x + 20;
        int contentY = bounds.y + 58;
        int contentWidth = bounds.width - 40;
        g2.setFont(new Font("Dialog", Font.PLAIN, 13));
        if (menuController.getTabIndex() == 0) {
            drawStatsMenu(g2, playerData, contentX, contentY, contentWidth, menuController.getStatsSelection(),
                    menuController.isSectionActive());
        } else if (menuController.getTabIndex() == 1) {
            drawInventoryMenu(g2, playerData, contentX, contentY, contentWidth,
                    menuController.getInventorySelection(), message);
        } else if (menuController.getTabIndex() == 2) {
            drawSkillsMenu(g2, playerData, mazeState, progression, contentX, contentY, contentWidth,
                    menuController, message);
        } else {
            drawLevelMenu(g2, playerData, contentX, contentY);
        }

        drawInteractionHint(g2, bounds, menuController);
        g2.dispose();
    }

    Rectangle menuBounds(int tabIndex) {
        boolean largeMenu = tabIndex == 0 || tabIndex == 1 || tabIndex == 2;
        int boxWidth = largeMenu ? Math.min(740, BASE_WIDTH - 24) : Math.min(620, BASE_WIDTH - 40);
        int boxHeight = largeMenu ? Math.min(520, BASE_HEIGHT - 34) : Math.min(420, BASE_HEIGHT - 40);
        int boxX = (BASE_WIDTH - boxWidth) / 2;
        int boxY = (BASE_HEIGHT - boxHeight) / 2;
        return new Rectangle(boxX, boxY, boxWidth, boxHeight);
    }

    Point contentOrigin(Rectangle bounds) {
        return new Point(bounds.x + 20, bounds.y + 58);
    }

    int tabAt(Point point, Rectangle bounds) {
        if (point.y < bounds.y || point.y > bounds.y + 34 || point.x < bounds.x || point.x > bounds.x + bounds.width) {
            return -1;
        }
        int tabWidth = bounds.width / TABS.length;
        return Math.max(0, Math.min(TABS.length - 1, (point.x - bounds.x) / tabWidth));
    }

    private void drawFrame(Graphics2D g2, Rectangle bounds, int tabIndex) {
        g2.setColor(PANEL);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        g2.setColor(PANEL_EDGE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        g2.setStroke(new BasicStroke(1));

        int tabWidth = bounds.width / TABS.length;
        g2.setFont(new Font("Dialog", Font.BOLD, 14));
        for (int i = 0; i < TABS.length; i++) {
            int tabX = bounds.x + i * tabWidth;
            g2.setColor(i == tabIndex ? TAB_ACTIVE : TAB);
            g2.fillRect(tabX, bounds.y, tabWidth, 34);
            g2.setColor(PANEL_EDGE);
            g2.drawRect(tabX, bounds.y, tabWidth, 34);
            g2.setColor(Color.WHITE);
            g2.drawString(TABS[i], tabX + 14, bounds.y + 22);
        }
    }

    private void drawInteractionHint(Graphics2D g2, Rectangle bounds, GameMenuController menuController) {
        g2.setFont(new Font("Dialog", Font.PLAIN, 11));
        g2.setColor(MUTED_TEXT);
        String hint = menuController.isSectionActive()
                ? "Arrows: move inside   Enter: action   Esc: back"
                : "Left/Right: tabs   Enter: open section   M/Esc: close";
        g2.drawString(hint, bounds.x + 18, bounds.y + bounds.height - 12);
    }

    private void drawStatsMenu(Graphics2D g2, PlayerData playerData, int x, int y, int width,
                               int statsSelection, boolean active) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        g2.drawString("Character Stats", x, y);
        g2.setFont(new Font("Dialog", Font.BOLD, 13));
        g2.drawString("Level " + playerData.getLevel(), x + width - 150, y - 2);
        g2.drawString("Points: " + playerData.getStatPoints(), x + width - 80, y - 2);
        drawVitalPanel(g2, playerData, x, y + 20, width);

        String[] labels = {"STR", "DEX", "INT", "DEF", "AGI", "LCK", "MIND", "CON"};
        String[] descriptions = {
            "Sword damage and physical power",
            "Dagger damage and evasive actions",
            "Spell damage and magic resistance",
            "Physical mitigation",
            "Turn speed and hit pressure",
            "Criticals and lucky outcomes",
            "Mana growth and spell focus",
            "Health growth and endurance"
        };
        int[] base = {
            playerData.getBaseStr(),
            playerData.getBaseDex(),
            playerData.getBaseInt(),
            playerData.getBaseDef(),
            playerData.getBaseAgi(),
            playerData.getBaseLuck(),
            playerData.getBaseMind(),
            playerData.getBaseCon()
        };
        int[] total = {
            playerData.getTotalStr(),
            playerData.getTotalDex(),
            playerData.getTotalInt(),
            playerData.getTotalDef(),
            playerData.getTotalAgi(),
            playerData.getTotalLuck(),
            playerData.getTotalMind(),
            playerData.getTotalCon()
        };
        int gridY = y + 88;
        int cardW = (width - 18) / 2;
        for (int i = 0; i < labels.length; i++) {
            int cardX = x + (i % 2) * (cardW + 18);
            int cardY = gridY + (i / 2) * 58;
            drawStatCard(g2, cardX, cardY, cardW, labels[i], descriptions[i], base[i], total[i],
                    active && statsSelection == i);
        }

        int equipmentY = gridY + 250;
        g2.setFont(new Font("Dialog", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        g2.drawString("Equipped bonuses", x, equipmentY);
        g2.setFont(new Font("Dialog", Font.PLAIN, 12));
        int line = equipmentY + 20;
        int shown = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item item = playerData.getEquippedItems().get(slot);
            if (item == null) {
                continue;
            }
            if (shown >= 5) {
                g2.setColor(MUTED_TEXT);
                g2.drawString("+ more equipped items", x, line);
                break;
            }
            g2.setColor(MUTED_TEXT);
            g2.drawString(slot.name(), x, line);
            g2.setColor(Color.WHITE);
            g2.drawString(item.getName() + "  " + item.getBonusSummary(), x + 110, line);
            line += 17;
            shown++;
        }
    }

    private void drawVitalPanel(Graphics2D g2, PlayerData playerData, int x, int y, int width) {
        g2.setColor(new Color(10, 10, 14, 180));
        g2.fillRoundRect(x, y, width, 46, 8, 8);
        g2.setColor(new Color(72, 72, 84));
        g2.drawRoundRect(x, y, width, 46, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 12));
        g2.drawString("HP " + playerData.getCurrentHp() + "/" + playerData.getMaxHp(), x + 14, y + 18);
        drawMiniBar(g2, x + 86, y + 9, 170, playerData.getCurrentHp(), playerData.getMaxHp());
        g2.drawString("MP " + playerData.getCurrentMp() + "/" + playerData.getMaxMp(), x + 300, y + 18);
        drawMiniBar(g2, x + 372, y + 9, 170, playerData.getCurrentMp(), playerData.getMaxMp());
        g2.drawString("XP " + playerData.getCurrentXp() + "/" + playerData.getNextLevelXp(), x + 14, y + 38);
        drawMiniBar(g2, x + 86, y + 29, 456, playerData.getCurrentXp(), playerData.getNextLevelXp());
    }

    private void drawStatCard(Graphics2D g2, int x, int y, int width, String label, String description,
                              int base, int total, boolean selected) {
        g2.setColor(selected ? new Color(82, 38, 30, 220) : new Color(22, 22, 30, 210));
        g2.fillRoundRect(x, y, width, 48, 8, 8);
        g2.setColor(selected ? SELECTION : new Color(78, 78, 92));
        g2.drawRoundRect(x, y, width, 48, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 15));
        g2.drawString(label, x + 10, y + 18);
        g2.setFont(new Font("Dialog", Font.BOLD, 12));
        g2.drawString(base + " -> " + total, x + width - 70, y + 18);
        drawMiniBar(g2, x + 10, y + 26, width - 20, total, 30);
        g2.setFont(new Font("Dialog", Font.PLAIN, 10));
        g2.setColor(MUTED_TEXT);
        g2.drawString(description, x + 10, y + 43);
    }

    private void drawMiniBar(Graphics2D g2, int x, int y, int width, int value, int maxValue) {
        g2.setColor(new Color(48, 48, 60));
        g2.fillRect(x, y, width, 8);
        g2.setColor(new Color(210, 175, 78));
        int fill = Math.min(width, Math.max(2, value * width / Math.max(1, maxValue)));
        g2.fillRect(x, y, fill, 8);
        g2.setColor(new Color(0, 0, 0, 130));
        g2.drawRect(x, y, width, 8);
    }

    private void drawSkillsMenu(Graphics2D g2, PlayerData playerData, MazeState mazeState,
                                SkillTreeProgression progression, int x, int y, int width,
                                GameMenuController menuController, String message) {
        skillTreeMenuRenderer.draw(g2, playerData, mazeState, progression, x, y - 12, width, CONTENT_HEIGHT,
                menuController.getSkillCursorNodeId(), message);
    }

    private void drawLevelMenu(Graphics2D g2, PlayerData playerData, int x, int y) {
        g2.drawString("Level: " + playerData.getLevel(), x, y);
        g2.drawString("XP: " + playerData.getCurrentXp() + " / " + playerData.getNextLevelXp(), x, y + 20);
        g2.drawString("Stat points: " + playerData.getStatPoints(), x, y + 40);
        g2.drawString("Skill points: " + playerData.getSkillPoints(), x, y + 60);
        g2.drawString("Win battles to gain XP.", x, y + 90);
    }

    private void drawInventoryMenu(Graphics2D g2, PlayerData playerData, int x, int y, int width,
                                   int inventorySelection, String message) {
        inventoryMenuRenderer.draw(g2, playerData, x, y - 12, width, CONTENT_HEIGHT, inventorySelection);
        if (message != null && !message.isEmpty()) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Dialog", Font.PLAIN, 11));
            g2.drawString(message, x + 12, y + 390);
        }
    }
}
