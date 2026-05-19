package game;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LaberintoPanel extends JPanel implements KeyListener {
    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private static final Color MENU_BACKDROP = new Color(0, 0, 0, 175);
    private static final Color MENU_PANEL = new Color(18, 18, 24, 238);
    private static final Color MENU_PANEL_EDGE = new Color(230, 220, 165);
    private static final Color MENU_TAB = new Color(35, 35, 48);
    private static final Color MENU_TAB_ACTIVE = new Color(88, 56, 44);
    private static final Color MENU_SELECTION = new Color(118, 56, 42);
    private static final Color MENU_MUTED_TEXT = new Color(170, 170, 180);
    private final GameHost gameHost;
    private MazeState mazeState;
    private MazeController mazeController;
    private MazeRenderer mazeRenderer;
    private InventoryMenuRenderer inventoryMenuRenderer;
    private SkillTreeMenuRenderer skillTreeMenuRenderer;
    private SkillTreeProgression skillTreeProgression;
    private CombatEnemy pendingCombatEnemy;
    private String menuMessage = "";
    private boolean isFightingGameStarted = false;
    private boolean inCombat = false;
    private boolean menuOpen = false;
    private int menuTabIndex = 0;
    private int inventorySelection = 0;
    private int statsSelection = 0;
    private int skillCategoryIndex = 0;
    private int skillSelectionIndex = 0;
    private Timer enemyTimer;
    private final PlayerData playerData;
    private int nivelActual = 1;
    private int saveCounter = 1; // Counter for generating unique save file names

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Laberinto Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            LaberintoPanel laberintoPanel = new LaberintoPanel();
            frame.add(laberintoPanel);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            
            JMenuBar menuBar = new JMenuBar(); // Create a menu bar
            
            // Create a menu for game actions
            JMenu gameMenu = new JMenu("Game");
            
            // Create menu items for saving, loading, and exiting the game
            JMenuItem saveItem = new JMenuItem("Save Game");
            saveItem.addActionListener(e -> {
                try {
                    laberintoPanel.guardarPartida();
                    System.out.println("Game saved");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            
            JMenuItem loadItem = new JMenuItem("Load Game");
            loadItem.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
                
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
                fileChooser.setFileFilter(filter);
                
                int result = fileChooser.showOpenDialog(null);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        laberintoPanel.recuperarPartida(selectedFile);
                        System.out.println("Game loaded");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            
            // Add menu items to the game menu
            gameMenu.add(saveItem);
            gameMenu.add(loadItem);
            gameMenu.addSeparator(); // Add a separator line
            gameMenu.add(exitItem);
            
            // Add the game menu to the menu bar
            menuBar.add(gameMenu);
            
            // Set the menu bar for the frame
            frame.setJMenuBar(menuBar);
            
            frame.setVisible(true);
        });
    }
    
    public LaberintoPanel() {
        this(null);
    }

    public LaberintoPanel(GameHost gameHost) {
        this.gameHost = gameHost;
        setFocusable(true);
        addKeyListener(this);
        playerData = new PlayerData();
        mazeState = new MazeState();
        mazeRenderer = new MazeRenderer();
        inventoryMenuRenderer = new InventoryMenuRenderer();
        skillTreeMenuRenderer = new SkillTreeMenuRenderer();
        skillTreeProgression = new SkillTreeProgression();
        mazeController = new MazeController(mazeState, new MazeController.Listener() {
            @Override
            public void onCombatRequested(CombatEnemy enemy) {
                startCombat(enemy);
            }

            @Override
            public void onLevelExitRequested(int nextLevel) {
                nivelActual = nextLevel;
                cargarSiguienteLaberinto();
            }

            @Override
            public void onMazeChanged() {
                repaint();
            }
        });
        nivelActual = mazeState.getLevel();
        // Schedule a task to move the enemy periodically
        enemyTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mazeController.moveEnemy();
                repaint();
            }
        });
        enemyTimer.start();
    }


    /*
    private void moverEnemigo() {
        if (inCombat || menuOpen || !enemyAlive) {
            return;
        }
        // Lógica de movimiento del enemigo (puedes personalizar esto según tus necesidades)
        Random random = new Random();
        int direccion = random.nextInt(4); // 0: arriba, 1: abajo, 2: izquierda, 3: derecha
    
        int nuevaX = enemigo.getRow();
        int nuevaY = enemigo.getColumn();
    
        switch (direccion) {
            case 0: // Arriba
                nuevaX = Math.max(0, nuevaX - 1);
                break;
            case 1: // Abajo
                nuevaX = Math.min(laberinto.length - 1, nuevaX + 1);
                break;
            case 2: // Izquierda
                nuevaY = Math.max(0, nuevaY - 1);
                break;
            case 3: // Derecha
                nuevaY = Math.min(laberinto[0].length - 1, nuevaY + 1);
                break;
        }
    
        // Verificar que la nueva posición sea válida antes de actualizarla
        if (laberinto[nuevaX][nuevaY] == 1) {
            // Restaurar la posición actual del enemigo en el laberinto
            laberinto[enemigo.getRow()][enemigo.getColumn()] = 1;
    
            enemigo.setPosition(nuevaX, nuevaY);
    
            // Actualizar la posición del enemigo en el laberinto
            laberinto[nuevaX][nuevaY] = 4;
            
        }
        if (enemigo.getRow() == posX && enemigo.getColumn() == posY) {
            startCombat();
        }
    }
    
    */
    private void startCombat() {
        startCombat(mazeState.getEnemy() == null ? CombatEnemy.forLevel(mazeState.getLevel(), false) : mazeState.getEnemy().getCombatEnemy());
    }

    private void syncLegacyFieldsFromState() {
        nivelActual = mazeState.getLevel();
    }

    private void startCombat(CombatEnemy combatEnemy) {
        if (isFightingGameStarted || inCombat) {
            return;
        }
        pendingCombatEnemy = combatEnemy;
        mazeState.savePlayerPosition();
        inCombat = true;
        isFightingGameStarted = true;
        if (enemyTimer != null) {
            enemyTimer.stop();
        }
        showCombatPanel();
    }

    private void showCombatPanel() {
        PanelTriangulo combatPanel = new PanelTriangulo(playerData, pendingCombatEnemy);
        combatPanel.setCombatEndListener(playerWon -> {
            resumeFromCombat(playerWon);
            if (gameHost != null) {
                gameHost.showMaze(this);
            } else {
                showMazeInCurrentFrame();
            }
        });
        if (gameHost != null) {
            gameHost.showCombat(combatPanel);
        } else {
            showCombatInCurrentFrame(combatPanel);
        }
    }

    private void showCombatInCurrentFrame(PanelTriangulo combatPanel) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame)) {
            inCombat = false;
            isFightingGameStarted = false;
            if (enemyTimer != null) {
                enemyTimer.start();
            }
            return;
        }
        JFrame frame = (JFrame) window;
        frame.getContentPane().removeAll();
        frame.getContentPane().add(combatPanel);
        frame.revalidate();
        frame.repaint();
        combatPanel.requestFocusInWindow();
    }

    private void showMazeInCurrentFrame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame)) {
            return;
        }
        JFrame frame = (JFrame) window;
        frame.getContentPane().removeAll();
        frame.getContentPane().add(this);
        frame.revalidate();
        frame.repaint();
        requestFocusInWindow();
    }

    private void resumeFromCombat(boolean playerWon) {
        inCombat = false;
        isFightingGameStarted = false;
        mazeState.restorePlayerPosition();
        if (playerWon) {
            mazeState.defeatEnemy();
        } else {
            repositionEnemyAwayFromPlayer();
        }
        syncLegacyFieldsFromState();
        if (enemyTimer != null) {
            enemyTimer.start();
        }
        if (!playerWon) {
            JOptionPane.showMessageDialog(this, "You lost the fight. Returning to the maze.");
        }
    }

    /*
    private void removeEnemyFromMaze() {
        int oldX = enemigo.getRow();
        int oldY = enemigo.getColumn();
        if (oldX >= 0 && oldX < laberinto.length && oldY >= 0 && oldY < laberinto[0].length) {
            if (laberinto[oldX][oldY] == 4) {
                laberinto[oldX][oldY] = 1;
            }
        }
        enemigo.defeat();
    }

    */
    private void repositionEnemyAwayFromPlayer() {
        Random random = new Random();
        int x = 1;
        int y = 1;
        do {
            x = random.nextInt(mazeState.getRows());
            y = random.nextInt(mazeState.getColumns());
        } while (mazeState.getTile(x, y) != TileType.FLOOR.getCode()
                || Math.abs(x - mazeState.getPlayerRow()) + Math.abs(y - mazeState.getPlayerColumn()) < 4);
        mazeState.moveEnemyTo(x, y);
    }

    /*
    private void colocarLetrasAleatorias() {
        Random random = new Random();
        int letrasColocadas = 0;

        while (letrasColocadas < 4) {
            int i = random.nextInt(laberinto.length);
            int j = random.nextInt(laberinto[0].length);

            if (laberinto[i][j] == 1) {
                laberinto[i][j] = 2; // Representaremos las letras con el número 2
                letrasColocadas++;
            }
        }
    }

    */
    void guardarPartida() throws IOException {
        // Incrementar las partidas guardadas para asegurar que hay mas de una cada vez que guardamos
        saveCounter++;

        // Generar archivo de guardado formato Año/Mes/Dia_HoraMinutosSegundos
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "partida_" + timeStamp + ".txt";

        // Remove the key listener temporarily to prevent duplicated key events
        removeKeyListener(this);
        
        try (FileOutputStream output = new FileOutputStream(fileName)) {
            Properties properties = mazeState.toProperties();
            skillTreeProgression.save(properties);
            properties.store(output, "Laberinto save");
/*
            // Guardar posicion del jugado, puntuacion y la generación del enemigo
            writer.write(posX + "\n");
            writer.write(posY + "\n");
            writer.write(puntuacion + "\n");
            writer.write(enemyRandomSeed + "\n"); // Save the random seed
    
            // Guardar el estado del laberinto actual
            for (int i = 0; i < laberinto.length; i++) {
                for (int j = 0; j < laberinto[i].length; j++) {
                    writer.write(laberinto[i][j] + " ");
                }
                writer.write("\n");
            }
    
            // Guardar posicion del enemigo
            writer.write(enemigo.getRow() + "\n");
            writer.write(enemigo.getColumn() + "\n");
*/
        }
    
        // Re-add the key listener after saving the game
        addKeyListener(this);
        
        // Ensure the panel has focus and repaint it
        requestFocusInWindow();
        repaint();
    }
    
    void recuperarPartida(File file) throws IOException {
        removeKeyListener(this);
        try (FileInputStream input = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(input);
            mazeState.load(properties);
            skillTreeProgression.load(properties);
            skillTreeProgression.reapplyPurchasedStats(playerData);
            syncLegacyFieldsFromState();
            addKeyListener(this);
        }
        requestFocusInWindow();
        repaint();
    }

    
    

    public int getPuntuacion() {
        return mazeState.getScore();
    }

        @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        double scaleX = (double) getWidth() / BASE_WIDTH;
        double scaleY = (double) getHeight() / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        int offsetX = (int) ((getWidth() - BASE_WIDTH * scale) / 2);
        int offsetY = (int) ((getHeight() - BASE_HEIGHT * scale) / 2);
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);
        mazeRenderer.draw(g2, mazeState, BASE_WIDTH, BASE_HEIGHT);
        if (menuOpen) {
            drawMenuOverlay(g2);
        }
        g2.dispose();
    }

    /*
    private void drawScene(Graphics2D g) {
        g.clearRect(0, 0, BASE_WIDTH, BASE_HEIGHT);
    
        // Draw the maze
        for (int i = 0; i < laberinto.length; i++) {
            for (int j = 0; j < laberinto[i].length; j++) {
                int x = j * 50; // x-coordinate of the cell
                int y = i * 50; // y-coordinate of the cell
                
                // Dibujar imagen correspondiente con la lista array del objeto
                switch (laberinto[i][j]) {
                    case 0: // Muro
                        g.drawImage(pared.getImage(), x, y, 50, 50, null);
                        break;
                    case 1: // Camino
                        g.drawImage(suelo.getImage(), x, y, 50, 50, null);
                        break;
                    case 2: // Letra (Monedas)
                        g.drawImage(palabra.getImage(), x, y, 50, 50, null);
                        break;
                    case 3: // Salida
                        g.drawImage(salida.getImage(), x, y, 50, 50, null);
                        break;
                    case 4: // Enemigo
                        g.drawImage(iconoEnemigo.getImage(), x, y, 50, 50, null);
                        break;
                    default:
                        break;
                }
            }
        }
    
        // Dibujar imagen del jugador
        g.drawImage(iconoJugador.getImage(), posY * 50, posX * 50, 50, 50, null);
    
        // Dibujar la puntuaci?n
        g.setColor(Color.BLACK);
        g.setFont(new Font("Roboto", Font.BOLD, 20));
        g.drawString("Monedas: " + puntuacion, 10, BASE_HEIGHT - 10);

        if (menuOpen) {
            drawMenuOverlay(g);
        }
    }
    
    
    private void recogerLetra() {
        if (laberinto[posX][posY] == 2) {
            puntuacion += 10;
            laberinto[posX][posY] = 1; 
            revalidate();
            repaint();
    
            // Comprobar que todas las letras han sido recogidas
            boolean allLetrasCollected = true;
            for (int i = 0; i < laberinto.length; i++) {
                for (int j = 0; j < laberinto[i].length; j++) {
                    if (laberinto[i][j] == 2) {
                        allLetrasCollected = false;
                        break;
                    }
                }
                if (!allLetrasCollected) {
                    break;
                }
            }
    
            if (allLetrasCollected) {
                // Notifcar al jugador que se han recogido todas las letras
                JOptionPane.showMessageDialog(this, "¡Has recogido todas las letras!");
    
                // Elegir un cuadrado aleatorio para que saque la salida
                int exitX, exitY;
                do {
                    exitX = (int) (Math.random() * laberinto.length);
                    exitY = (int) (Math.random() * laberinto[0].length);
                } while (laberinto[exitX][exitY] != 1);
    
                // Representar la salida como [3]
                laberinto[exitX][exitY] = 3;
                repaint();
            }
        } else if (laberinto[posX][posY] == 3) {
            // El jugador toca la salida
            // Notificar al jugador de que ha encontrado la salida y puede pasar al siguiente laberinto
            JOptionPane.showMessageDialog(this, "¡Has encontrado la salida! ¡Pasando al siguiente laberinto!");
    
            if (nivelActual < 6) { // Total de laberintos
                nivelActual++;
                cargarSiguienteLaberinto(); // Cargar laberinto
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "¡Has completado todos los laberintos!");
                // Si se completan todos los laberintos indicarlo con un mensaje
            }
        }
    }
    */
    private void cargarSiguienteLaberinto() {
        if (enemyTimer != null) {
            enemyTimer.stop();
        }

        String message = getLevelLoadingMessage();
        if (gameHost != null) {
            gameHost.showTemporaryScreen(new LevelLoadingPanel(message), 2000, () -> {
                finishLoadingNextMaze();
                gameHost.showMaze(this);
            });
            return;
        }

        finishLoadingNextMaze();
    }

    private String getLevelLoadingMessage() {
        if (nivelActual <= 5) {
            return "Mundo 1 - Nivel " + nivelActual;
        }
        return "Mundo 2 - Nivel " + (nivelActual - 5);
    }

    private void finishLoadingNextMaze() {
        mazeState.generateLevel(nivelActual);
        syncLegacyFieldsFromState();
        isFightingGameStarted = false;
/*
        if (nivelActual <= 5) {
            switch (nivelActual) {
                case 2:
                    setCurrentMaze(laberinto2);
                    break;
                case 3:
                    setCurrentMaze(laberinto3);
                    break;
                case 4:
                    setCurrentMaze(laberinto4);
                    break;
                case 5:
                    setCurrentMaze(laberinto5);
                    break;
                default:
                    
                    break;
            }
        } else {
             
            if (nivelActual == 6) {
                nivelActual = 1;
                setCurrentMaze(laberinto6);
            } else {
                
            }
        }
        enemyAlive = true;
        colocarEnemigoAleatorio();
    
        colocarLetrasAleatorias();
*/
        if (enemyTimer != null) {
            enemyTimer.start();
        }
        repaint();
    }
    
    
    private int jumpDirection = 0;

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed: " + e.getKeyCode());

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_M) {
            toggleMenu();
            return;
        }
        if (menuOpen) {
            handleMenuKey(key);
            return;
        }
        if (inCombat) {
            return;
        }
    
        mazeController.handleKey(key);
        syncLegacyFieldsFromState();
        repaint();
    }
    public static void startGame() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Laberinto Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            LaberintoPanel laberintoPanel = new LaberintoPanel();
            frame.add(laberintoPanel);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

/*
private void jump() {
    System.out.println("Jumping");

    int jumpPosY = posY + jumpDirection;

  
    if (laberinto[posX][posY] == 1) {
    
        if (jumpPosY >= 0 && jumpPosY < laberinto[0].length && laberinto[posX][jumpPosY] == 0) {
            
            int targetPosY = posY + 2 * jumpDirection;
            if (targetPosY >= 0 && targetPosY < laberinto[0].length && laberinto[posX][targetPosY] == 1) {
            
                posY = targetPosY;

                
                jumpDirection = 0;
            }
        }
    }
}

*/
    private void toggleMenu() {
        if (inCombat) {
            return;
        }
        menuOpen = !menuOpen;
        if (menuOpen) {
            if (enemyTimer != null) {
                enemyTimer.stop();
            }
        } else {
            if (enemyTimer != null) {
                enemyTimer.start();
            }
            requestFocusInWindow();
        }
        repaint();
    }

    private void handleMenuKey(int key) {
        if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_M) {
            toggleMenu();
            return;
        }
        if (key == KeyEvent.VK_LEFT) {
            menuTabIndex = Math.max(0, menuTabIndex - 1);
            resetMenuSelections();
        } else if (key == KeyEvent.VK_RIGHT) {
            menuTabIndex = Math.min(3, menuTabIndex + 1);
            resetMenuSelections();
        } else if (menuTabIndex == 0) {
            if (key == KeyEvent.VK_UP) {
                statsSelection = Math.max(0, statsSelection - 1);
            } else if (key == KeyEvent.VK_DOWN) {
                statsSelection = Math.min(5, statsSelection + 1);
            } else if (key == KeyEvent.VK_ENTER) {
                StatType statType = StatType.values()[statsSelection];
                playerData.allocateStatPoint(statType);
            }
        } else if (menuTabIndex == 1) {
            if (key == KeyEvent.VK_UP) {
                inventorySelection = Math.max(0, inventorySelection - 1);
            } else if (key == KeyEvent.VK_DOWN) {
                int maxIndex = Math.max(0, playerData.getInventoryItems().size() - 1);
                inventorySelection = Math.min(maxIndex, inventorySelection + 1);
            } else if (key == KeyEvent.VK_ENTER) {
                playerData.equipItem(inventorySelection);
                int maxIndex = Math.max(0, playerData.getInventoryItems().size() - 1);
                inventorySelection = Math.min(inventorySelection, maxIndex);
            }
        } else if (menuTabIndex == 2) {
            if (key == KeyEvent.VK_UP) {
                skillSelectionIndex = Math.max(0, skillSelectionIndex - 1);
            } else if (key == KeyEvent.VK_DOWN) {
                int maxIndex = Math.max(0, playerData.getSkillTree().getSkillsByCategory(getCurrentSkillCategory()).size() - 1);
                skillSelectionIndex = Math.min(maxIndex, skillSelectionIndex + 1);
            } else if (key == KeyEvent.VK_A) {
                skillCategoryIndex = Math.max(0, skillCategoryIndex - 1);
                clampSkillSelection();
            } else if (key == KeyEvent.VK_D) {
                skillCategoryIndex = Math.min(SkillCategory.values().length - 1, skillCategoryIndex + 1);
                clampSkillSelection();
            } else if (key == KeyEvent.VK_ENTER) {
                PurchaseResult result = skillTreeProgression.purchaseSelected(playerData, mazeState, getCurrentSkillCategory(), skillSelectionIndex);
                menuMessage = result.getMessage();
            }
        }
        repaint();
    }

    private void resetMenuSelections() {
        statsSelection = 0;
        inventorySelection = 0;
        skillSelectionIndex = 0;
    }

    private void clampSkillSelection() {
        int maxIndex = Math.max(0, playerData.getSkillTree().getSkillsByCategory(getCurrentSkillCategory()).size() - 1);
        skillSelectionIndex = Math.min(skillSelectionIndex, maxIndex);
    }

    private SkillCategory getCurrentSkillCategory() {
        SkillCategory[] categories = SkillCategory.values();
        if (skillCategoryIndex < 0 || skillCategoryIndex >= categories.length) {
            return SkillCategory.ATTACK;
        }
        return categories[skillCategoryIndex];
    }

    private Skill getSelectedSkill() {
        List<Skill> skills = playerData.getSkillTree().getSkillsByCategory(getCurrentSkillCategory());
        if (skills.isEmpty()) {
            return null;
        }
        if (skillSelectionIndex < 0 || skillSelectionIndex >= skills.size()) {
            return skills.get(0);
        }
        return skills.get(skillSelectionIndex);
    }

    private void drawMenuOverlay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(MENU_BACKDROP);
        g2.fillRect(0, 0, BASE_WIDTH, BASE_HEIGHT);

        boolean largeMenu = menuTabIndex == 1 || menuTabIndex == 2;
        int boxWidth = largeMenu ? Math.min(740, BASE_WIDTH - 24) : Math.min(620, BASE_WIDTH - 40);
        int boxHeight = largeMenu ? Math.min(520, BASE_HEIGHT - 34) : Math.min(420, BASE_HEIGHT - 40);
        int boxX = (BASE_WIDTH - boxWidth) / 2;
        int boxY = (BASE_HEIGHT - boxHeight) / 2;

        g2.setColor(MENU_PANEL);
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);
        g2.setColor(MENU_PANEL_EDGE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);
        g2.setStroke(new BasicStroke(1));

        String[] tabs = {"Stats", "Inventory", "Skills", "Floor"};
        int tabWidth = boxWidth / tabs.length;
        g2.setFont(new Font("Dialog", Font.BOLD, 14));
        for (int i = 0; i < tabs.length; i++) {
            int tabX = boxX + i * tabWidth;
            g2.setColor(i == menuTabIndex ? MENU_TAB_ACTIVE : MENU_TAB);
            g2.fillRect(tabX, boxY, tabWidth, 34);
            g2.setColor(MENU_PANEL_EDGE);
            g2.drawRect(tabX, boxY, tabWidth, 34);
            g2.setColor(Color.WHITE);
            g2.drawString(tabs[i], tabX + 14, boxY + 22);
        }

        int contentX = boxX + 20;
        int contentY = boxY + 58;
        g2.setFont(new Font("Dialog", Font.PLAIN, 13));
        if (menuTabIndex == 0) {
            drawStatsMenu(g2, contentX, contentY);
        } else if (menuTabIndex == 1) {
            drawInventoryMenu(g2, contentX, contentY, boxWidth - 40);
        } else if (menuTabIndex == 2) {
            drawSkillsMenu(g2, contentX, contentY, boxWidth - 40);
        } else {
            drawLevelMenu(g2, contentX, contentY);
        }

        g2.dispose();
    }

    private void drawStatsMenu(Graphics2D g2, int x, int y) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 16));
        g2.drawString("Base / Total", x, y);
        g2.drawString("Stat points: " + playerData.getStatPoints(), x + 210, y);
        g2.setFont(new Font("Dialog", Font.PLAIN, 13));
        int line = y + 20;
        String[] labels = {"STR", "DEF", "AGI", "LCK", "MIN", "CON"};
        int[] base = {
            playerData.getBaseStr(),
            playerData.getBaseDef(),
            playerData.getBaseAgi(),
            playerData.getBaseLuck(),
            playerData.getBaseMind(),
            playerData.getBaseCon()
        };
        int[] total = {
            playerData.getTotalStr(),
            playerData.getTotalDef(),
            playerData.getTotalAgi(),
            playerData.getTotalLuck(),
            playerData.getTotalMind(),
            playerData.getTotalCon()
        };
        for (int i = 0; i < labels.length; i++) {
            if (statsSelection == i) {
                g2.setColor(MENU_SELECTION);
                g2.fillRoundRect(x - 8, line - 14, 180, 19, 6, 6);
            }
            g2.setColor(Color.WHITE);
            g2.drawString(labels[i] + ": " + base[i] + " / " + total[i], x, line);
            drawMiniBar(g2, x + 86, line - 10, 70, total[i], 20);
            line += 22;
        }
        g2.setColor(Color.WHITE);
        g2.drawString("HP: " + playerData.getCurrentHp() + " / " + playerData.getMaxHp(), x, line);
        line += 20;
        g2.drawString("MP: " + playerData.getCurrentMp() + " / " + playerData.getMaxMp(), x, line);
        line += 20;
        g2.setColor(MENU_MUTED_TEXT);
        g2.drawString("Enter: Spend point   M/Esc: Close", x, line);

        line = y + 20;
        int equipmentX = x + 320;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 16));
        g2.drawString("Equipped", equipmentX, y);
        g2.setFont(new Font("Dialog", Font.PLAIN, 13));
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item item = playerData.getEquippedItems().get(slot);
            String name = item == null ? "-" : item.getName();
            g2.setColor(MENU_MUTED_TEXT);
            g2.drawString(slot.name(), equipmentX, line);
            g2.setColor(Color.WHITE);
            g2.drawString(name, equipmentX + 105, line);
            line += 18;
        }
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

    private void drawSkillsMenu(Graphics2D g2, int x, int y, int width) {
        skillTreeMenuRenderer.draw(g2, playerData, mazeState, skillTreeProgression, x, y - 12, width, 400,
                skillCategoryIndex, skillSelectionIndex, menuMessage);
    }

    private String formatSkillCategory(SkillCategory category) {
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

    private void drawLevelMenu(Graphics2D g2, int x, int y) {
        g2.drawString("Level: " + playerData.getLevel(), x, y);
        g2.drawString("XP: " + playerData.getCurrentXp() + " / " + playerData.getNextLevelXp(), x, y + 20);
        g2.drawString("Stat points: " + playerData.getStatPoints(), x, y + 40);
        g2.drawString("Skill points: " + playerData.getSkillPoints(), x, y + 60);
        g2.drawString("Win battles to gain XP.", x, y + 90);
    }

    private void drawInventoryMenu(Graphics2D g2, int x, int y, int width) {
        inventoryMenuRenderer.draw(g2, playerData, x, y - 12, width, 400, inventorySelection);
    }



    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void loadGame(File selectedFile) throws IOException {
        removeKeyListener(this);
        try (FileInputStream input = new FileInputStream(selectedFile)) {
            Properties properties = new Properties();
            properties.load(input);
            mazeState.load(properties);
            skillTreeProgression.load(properties);
            skillTreeProgression.reapplyPurchasedStats(playerData);
            syncLegacyFieldsFromState();
            addKeyListener(this);
        }
        requestFocusInWindow();
        repaint();
    }
}

