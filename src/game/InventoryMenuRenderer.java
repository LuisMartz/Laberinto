package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class InventoryMenuRenderer {
    private static final Color PANEL = new Color(18, 15, 12, 232);
    private static final Color PANEL_DARK = new Color(8, 7, 6, 220);
    private static final Color GOLD = new Color(205, 176, 105);
    private static final Color GOLD_DIM = new Color(125, 103, 62);
    private static final Color TEXT = new Color(238, 230, 202);
    private static final Color MUTED = new Color(166, 154, 124);
    private static final Color SELECT = new Color(173, 78, 47);
    private static final Color EMPTY_SLOT = new Color(20, 18, 16);

    public void draw(Graphics2D g, PlayerData playerData, int x, int y, int width, int height, int selectedIndex) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int gap = 18;
        int bagWidth = Math.min(300, width / 2 - gap);
        int characterX = x + bagWidth + gap;
        int characterWidth = width - bagWidth - gap;

        drawBag(g2, playerData.getInventoryItems(), x, y, bagWidth, height, selectedIndex);
        drawCharacterPanel(g2, playerData, characterX, y, characterWidth, height);

        g2.dispose();
    }

    public int hitTestBagIndex(int mouseX, int mouseY, int x, int y, int width, int height, int itemCount) {
        int gap = 18;
        int bagWidth = Math.min(300, width / 2 - gap);
        int columns = 5;
        int rows = 6;
        int cell = Math.min(42, (bagWidth - 28) / columns);
        int gridX = x + 14;
        int gridY = y + 34;

        if (mouseX < gridX || mouseY < gridY || mouseX >= gridX + columns * cell || mouseY >= gridY + rows * cell) {
            return -1;
        }
        int column = (mouseX - gridX) / cell;
        int row = (mouseY - gridY) / cell;
        int index = row * columns + column;
        return index >= 0 && index < itemCount ? index : -1;
    }

    private void drawBag(Graphics2D g2, List<Item> items, int x, int y, int width, int height, int selectedIndex) {
        drawPanel(g2, x, y, width, height);
        drawTitle(g2, "INVENTORY", x + 12, y + 22);

        int columns = 5;
        int rows = 6;
        int cell = Math.min(42, (width - 28) / columns);
        int gridX = x + 14;
        int gridY = y + 34;

        for (int i = 0; i < columns * rows; i++) {
            int col = i % columns;
            int row = i / columns;
            int cellX = gridX + col * cell;
            int cellY = gridY + row * cell;
            boolean selected = i == selectedIndex && i < items.size();
            drawSlot(g2, cellX, cellY, cell - 3, cell - 3, selected);
            if (i < items.size()) {
                drawItemIcon(g2, items.get(i), cellX, cellY, cell - 3, cell - 3);
            }
        }

        int infoY = gridY + rows * cell + 16;
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(TEXT);
        if (!items.isEmpty() && selectedIndex >= 0 && selectedIndex < items.size()) {
            Item item = items.get(selectedIndex);
            g2.drawString(item.getName(), x + 14, infoY);
            g2.setFont(new Font("Dialog", Font.PLAIN, 11));
            g2.setColor(MUTED);
            g2.drawString(item.getSlot().name() + "  " + item.getBonusSummary(), x + 14, infoY + 17);
        } else {
            g2.setColor(MUTED);
            g2.drawString("Bag is empty", x + 14, infoY);
        }

        drawBagTabs(g2, x + 12, y + height - 34, width - 24);
    }

    private void drawCharacterPanel(Graphics2D g2, PlayerData playerData, int x, int y, int width, int height) {
        drawPanel(g2, x, y, width, height);
        drawTitle(g2, "CHARACTER", x + width - 116, y + height - 14);
        drawRelicLabel(g2, x + 18, y + 84);
        drawBodySilhouette(g2, x + width / 2, y + 190);

        Map<EquipmentSlot, SlotBox> boxes = createEquipmentLayout(x, y, width);
        Map<EquipmentSlot, Item> equipped = playerData.getEquippedItems();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            SlotBox box = boxes.get(slot);
            if (box == null) {
                continue;
            }
            drawSlot(g2, box.x, box.y, box.size, box.size, false);
            Item item = equipped.get(slot);
            if (item != null) {
                drawItemIcon(g2, item, box.x, box.y, box.size, box.size);
            }
            g2.setFont(new Font("Dialog", Font.PLAIN, 9));
            g2.setColor(MUTED);
            drawCentered(g2, slotLabel(slot), box.x + box.size / 2, box.y + box.size + 11);
        }

        g2.setColor(GOLD_DIM);
        g2.setStroke(new BasicStroke(1));
        connect(g2, boxes.get(EquipmentSlot.HEAD), x + width / 2, y + 92);
        connect(g2, boxes.get(EquipmentSlot.ARMOR), x + width / 2, y + 176);
        connect(g2, boxes.get(EquipmentSlot.WEAPON), x + width / 2 - 54, y + 164);
        connect(g2, boxes.get(EquipmentSlot.LEFT_HAND), x + width / 2 + 52, y + 165);
        connect(g2, boxes.get(EquipmentSlot.BOOTS), x + width / 2, y + 278);
    }

    private Map<EquipmentSlot, SlotBox> createEquipmentLayout(int x, int y, int width) {
        int center = x + width / 2;
        int s = 42;
        Map<EquipmentSlot, SlotBox> boxes = new EnumMap<>(EquipmentSlot.class);
        boxes.put(EquipmentSlot.HEAD, new SlotBox(center - s / 2, y + 24, s));
        boxes.put(EquipmentSlot.ARMOR, new SlotBox(center - s / 2, y + 112, s));
        boxes.put(EquipmentSlot.GEAR, new SlotBox(center - s / 2, y + 164, s));
        boxes.put(EquipmentSlot.BOOTS, new SlotBox(center - s / 2, y + 274, s));
        boxes.put(EquipmentSlot.WEAPON, new SlotBox(x + 22, y + 170, s));
        boxes.put(EquipmentSlot.LEFT_HAND, new SlotBox(x + width - s - 22, y + 170, s));
        boxes.put(EquipmentSlot.RIGHT_HAND, new SlotBox(x + 58, y + 92, s));
        boxes.put(EquipmentSlot.RING, new SlotBox(x + width - s - 58, y + 92, s));
        boxes.put(EquipmentSlot.ACCESSORY, new SlotBox(x + 22, y + 22, s));
        return boxes;
    }

    private void drawPanel(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(PANEL);
        g2.fillRect(x, y, width, height);
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, width, height);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(70, 57, 34, 150));
        for (int i = 0; i < 5; i++) {
            g2.drawRect(x + 4 + i, y + 4 + i, width - 8 - i * 2, height - 8 - i * 2);
        }
    }

    private void drawTitle(Graphics2D g2, String title, int x, int y) {
        g2.setFont(new Font("Serif", Font.BOLD, 15));
        g2.setColor(TEXT);
        g2.drawString(title, x, y);
    }

    private void drawSlot(Graphics2D g2, int x, int y, int width, int height, boolean selected) {
        g2.setColor(selected ? new Color(70, 32, 24) : EMPTY_SLOT);
        g2.fillRect(x, y, width, height);
        g2.setColor(selected ? SELECT : GOLD);
        g2.setStroke(new BasicStroke(selected ? 3 : 2));
        g2.drawRect(x, y, width, height);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(PANEL_DARK);
        g2.drawRect(x + 4, y + 4, width - 8, height - 8);
    }

    private void drawItemIcon(Graphics2D g2, Item item, int x, int y, int width, int height) {
        Color color = colorForSlot(item.getSlot());
        g2.setColor(color);
        g2.fillRoundRect(x + 8, y + 8, width - 16, height - 16, 8, 8);
        g2.setColor(new Color(255, 246, 202, 190));
        g2.drawRoundRect(x + 8, y + 8, width - 16, height - 16, 8, 8);
        g2.setFont(new Font("Serif", Font.BOLD, 14));
        g2.setColor(Color.BLACK);
        drawCentered(g2, abbreviation(item), x + width / 2, y + height / 2 + 5);
    }

    private Color colorForSlot(EquipmentSlot slot) {
        switch (slot) {
            case WEAPON:
            case RIGHT_HAND:
            case LEFT_HAND:
                return new Color(178, 159, 92);
            case ARMOR:
            case HEAD:
            case BOOTS:
                return new Color(94, 114, 138);
            case RING:
            case ACCESSORY:
                return new Color(128, 89, 153);
            case GEAR:
                return new Color(111, 139, 92);
            default:
                return new Color(130, 100, 70);
        }
    }

    private String abbreviation(Item item) {
        String name = item.getName().trim();
        if (name.isEmpty()) {
            return "?";
        }
        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void drawBodySilhouette(Graphics2D g2, int centerX, int centerY) {
        g2.setColor(new Color(50, 43, 36, 190));
        g2.fillOval(centerX - 20, centerY - 122, 40, 42);
        g2.fillRoundRect(centerX - 32, centerY - 78, 64, 106, 22, 22);
        g2.fillRoundRect(centerX - 62, centerY - 52, 28, 96, 14, 14);
        g2.fillRoundRect(centerX + 34, centerY - 52, 28, 96, 14, 14);
        g2.fillRoundRect(centerX - 30, centerY + 26, 24, 96, 12, 12);
        g2.fillRoundRect(centerX + 6, centerY + 26, 24, 96, 12, 12);
        g2.setColor(new Color(214, 183, 105, 70));
        g2.drawOval(centerX - 20, centerY - 122, 40, 42);
        g2.drawRoundRect(centerX - 32, centerY - 78, 64, 106, 22, 22);
    }

    private void drawRelicLabel(Graphics2D g2, int x, int y) {
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        g2.setColor(TEXT);
        g2.drawString("RELICS", x, y);
    }

    private void drawBagTabs(Graphics2D g2, int x, int y, int width) {
        String[] tabs = {"BAG", "BAG 2", "BAG 3", "BAG 4"};
        int tabWidth = width / tabs.length;
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        for (int i = 0; i < tabs.length; i++) {
            int tabX = x + i * tabWidth;
            g2.setColor(i == 0 ? new Color(52, 42, 30) : new Color(35, 30, 24));
            g2.fillRect(tabX, y, tabWidth - 3, 24);
            g2.setColor(GOLD);
            g2.drawRect(tabX, y, tabWidth - 3, 24);
            g2.setColor(TEXT);
            drawCentered(g2, tabs[i], tabX + (tabWidth - 3) / 2, y + 17);
        }
    }

    private String slotLabel(EquipmentSlot slot) {
        switch (slot) {
            case RIGHT_HAND:
                return "R HAND";
            case LEFT_HAND:
                return "L HAND";
            default:
                return slot.name();
        }
    }

    private void connect(Graphics2D g2, SlotBox box, int targetX, int targetY) {
        if (box == null) {
            return;
        }
        g2.drawLine(box.x + box.size / 2, box.y + box.size / 2, targetX, targetY);
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int baselineY) {
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private static class SlotBox {
        private final int x;
        private final int y;
        private final int size;

        private SlotBox(int x, int y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }
}
