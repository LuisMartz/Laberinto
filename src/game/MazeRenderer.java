package game;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class MazeRenderer {
    private static final int TILE_SIZE = 50;
    private final ImageIcon wall = AssetLoader.icon("tiles/pared.png");
    private final ImageIcon floor = AssetLoader.icon("tiles/suelo.png");
    private final ImageIcon coin = AssetLoader.icon("tiles/palabra.png");
    private final ImageIcon exit = AssetLoader.icon("tiles/salida.png");
    private final ImageIcon player = AssetLoader.icon("player/Larry.png");
    private final ImageIcon enemy = AssetLoader.icon("enemies/Enemigo.png");

    public void draw(Graphics2D g, MazeState state, int baseWidth, int baseHeight) {
        g.clearRect(0, 0, baseWidth, baseHeight);
        int rows = state.getRows();
        int columns = state.getColumns();
        int tileSize = Math.max(24, Math.min(TILE_SIZE, Math.min((baseWidth - 40) / columns, (baseHeight - 90) / rows)));
        int offsetX = (baseWidth - columns * tileSize) / 2;
        int offsetY = 24;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int x = offsetX + column * tileSize;
                int y = offsetY + row * tileSize;
                drawTile(g, state.getTile(row, column), x, y, tileSize);
            }
        }

        g.drawImage(player.getImage(), offsetX + state.getPlayerColumn() * tileSize, offsetY + state.getPlayerRow() * tileSize, tileSize, tileSize, null);
        drawHud(g, state, baseHeight);
    }

    private void drawTile(Graphics2D g, int tile, int x, int y, int size) {
        if (tile == TileType.WALL.getCode()) {
            g.drawImage(wall.getImage(), x, y, size, size, null);
        } else if (tile == TileType.COIN.getCode()) {
            g.drawImage(floor.getImage(), x, y, size, size, null);
            g.drawImage(coin.getImage(), x, y, size, size, null);
        } else if (tile == TileType.EXIT.getCode()) {
            g.drawImage(floor.getImage(), x, y, size, size, null);
            g.drawImage(exit.getImage(), x, y, size, size, null);
        } else if (tile == TileType.ENEMY.getCode()) {
            g.drawImage(floor.getImage(), x, y, size, size, null);
            g.drawImage(enemy.getImage(), x, y, size, size, null);
        } else {
            g.drawImage(floor.getImage(), x, y, size, size, null);
        }
    }

    private void drawHud(Graphics2D g, MazeState state, int baseHeight) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        g.drawString("Monedas: " + state.getScore(), 12, baseHeight - 16);
        g.drawString("Nivel: " + state.getLevel(), 170, baseHeight - 16);
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        g.drawString("M: menu   Flechas: mover", 290, baseHeight - 16);
    }
}
