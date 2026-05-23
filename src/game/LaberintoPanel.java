package game;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LaberintoPanel extends JPanel implements KeyListener {
    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private final GameHost gameHost;
    private MazeState mazeState;
    private MazeScreenController screenController;
    private MazeRenderer mazeRenderer;
    private InventoryMenuRenderer inventoryMenuRenderer;
    private SkillTreeMenuRenderer skillTreeMenuRenderer;
    private GameMenuController gameMenuController;
    private GameMenuRenderer gameMenuRenderer;
    private SkillTreeProgression skillTreeProgression;
    private SaveGameService saveGameService;
    private String menuMessage = "";
    private boolean menuOpen = false;
    private Timer enemyTimer;
    private final PlayerData playerData;

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
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
        });
        playerData = new PlayerData();
        mazeState = new MazeState();
        mazeRenderer = new MazeRenderer();
        inventoryMenuRenderer = new InventoryMenuRenderer();
        skillTreeMenuRenderer = new SkillTreeMenuRenderer();
        gameMenuController = new GameMenuController();
        gameMenuRenderer = new GameMenuRenderer(inventoryMenuRenderer, skillTreeMenuRenderer);
        skillTreeProgression = new SkillTreeProgression();
        saveGameService = new SaveGameService();
        screenController = new MazeScreenController(mazeState, playerData, new MazeScreenController.View() {
            @Override
            public void repaintMaze() {
                repaint();
            }

            @Override
            public void pauseEnemyMovement() {
                if (enemyTimer != null) {
                    enemyTimer.stop();
                }
            }

            @Override
            public void resumeEnemyMovement() {
                if (enemyTimer != null) {
                    enemyTimer.start();
                }
            }

            @Override
            public boolean showCombatPanel(PanelTriangulo combatPanel) {
                return showCombatInHostOrFrame(combatPanel);
            }

            @Override
            public void showMazePanel() {
                showMazeInHostOrFrame();
            }

            @Override
            public void showLevelLoading(String message, Runnable onComplete) {
                showLevelLoadingInHostOrRun(message, onComplete);
            }

            @Override
            public void showMessage(String message) {
                JOptionPane.showMessageDialog(LaberintoPanel.this, message);
            }

            @Override
            public void setMenuMessage(String message) {
                menuMessage = message;
            }
        });
        // Schedule a task to move the enemy periodically
        enemyTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                screenController.moveEnemy();
            }
        });
        enemyTimer.start();
    }


    private boolean showCombatInHostOrFrame(PanelTriangulo combatPanel) {
        if (gameHost != null) {
            gameHost.showCombat(combatPanel);
            return true;
        } else {
            return showCombatInCurrentFrame(combatPanel);
        }
    }

    private boolean showCombatInCurrentFrame(PanelTriangulo combatPanel) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame)) {
            return false;
        }
        JFrame frame = (JFrame) window;
        frame.getContentPane().removeAll();
        frame.getContentPane().add(combatPanel);
        frame.revalidate();
        frame.repaint();
        combatPanel.requestFocusInWindow();
        return true;
    }

    private void showMazeInHostOrFrame() {
        if (gameHost != null) {
            gameHost.showMaze(this);
            return;
        }
        showMazeInCurrentFrame();
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

    void guardarPartida() throws IOException {
        removeKeyListener(this);
        
        try {
            saveGameService.save(saveGameService.createTimestampedSaveFile(), mazeState, skillTreeProgression, playerData);
        } finally {
            addKeyListener(this);
            requestFocusInWindow();
            repaint();
        }
    }
    
    void recuperarPartida(File file) throws IOException {
        loadGame(file);
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
            gameMenuRenderer.draw(g2, playerData, mazeState, skillTreeProgression, gameMenuController, menuMessage);
        }
        g2.dispose();
    }

    private void showLevelLoadingInHostOrRun(String message, Runnable onComplete) {
        if (gameHost != null) {
            gameHost.showTemporaryScreen(new LevelLoadingPanel(message), 2000, () -> {
                onComplete.run();
                gameHost.showMaze(this);
            });
            return;
        }
        onComplete.run();
    }
    
    

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
        if (screenController.isInCombat()) {
            return;
        }
    
        screenController.handleMazeKey(key);
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

    private void toggleMenu() {
        if (screenController.isInCombat()) {
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
            gameMenuController.previousTab();
        } else if (key == KeyEvent.VK_RIGHT) {
            gameMenuController.nextTab();
        } else if (gameMenuController.getTabIndex() == 0) {
            if (key == KeyEvent.VK_UP) {
                gameMenuController.previousStat();
            } else if (key == KeyEvent.VK_DOWN) {
                gameMenuController.nextStat();
            } else if (key == KeyEvent.VK_ENTER) {
                playerData.allocateStatPoint(gameMenuController.getSelectedStat());
            }
        } else if (gameMenuController.getTabIndex() == 1) {
            if (key == KeyEvent.VK_UP) {
                gameMenuController.previousInventoryItem();
            } else if (key == KeyEvent.VK_DOWN) {
                gameMenuController.nextInventoryItem(playerData.getInventoryItems().size());
            } else if (key == KeyEvent.VK_ENTER) {
                playerData.equipItem(gameMenuController.getInventorySelection());
                gameMenuController.setInventorySelection(gameMenuController.getInventorySelection(),
                        playerData.getInventoryItems().size());
            }
        } else if (gameMenuController.getTabIndex() == 2) {
            if (key == KeyEvent.VK_UP) {
                gameMenuController.previousSkill();
            } else if (key == KeyEvent.VK_DOWN) {
                gameMenuController.nextSkill(playerData);
            } else if (key == KeyEvent.VK_A) {
                gameMenuController.previousSkillCategory(playerData);
            } else if (key == KeyEvent.VK_D) {
                gameMenuController.nextSkillCategory(playerData);
            } else if (key == KeyEvent.VK_ENTER) {
                PurchaseResult result = skillTreeProgression.purchaseSelected(playerData, mazeState,
                        gameMenuController.getCurrentSkillCategory(), gameMenuController.getSkillSelectionIndex());
                menuMessage = result.getMessage();
            }
        }
        repaint();
    }

    private void handleMousePressed(MouseEvent event) {
        requestFocusInWindow();
        if (!menuOpen) {
            return;
        }
        Point point = toBasePoint(event.getX(), event.getY());
        if (point == null) {
            return;
        }

        Rectangle bounds = gameMenuRenderer.menuBounds(gameMenuController.getTabIndex());
        int tabIndex = gameMenuRenderer.tabAt(point, bounds);
        if (tabIndex >= 0) {
            gameMenuController.setTabIndex(tabIndex);
            repaint();
            return;
        }

        if (gameMenuController.getTabIndex() == 1) {
            Point content = gameMenuRenderer.contentOrigin(bounds);
            int contentX = content.x;
            int contentY = content.y;
            int contentWidth = bounds.width - 40;
            EquipmentSlot slot = inventoryMenuRenderer.hitTestEquipmentSlot(point.x, point.y, contentX, contentY - 12,
                    contentWidth, GameMenuRenderer.CONTENT_HEIGHT);
            if (slot != null) {
                if (playerData.unequipItem(slot)) {
                    gameMenuController.setInventorySelection(playerData.getInventoryItems().size() - 1,
                            playerData.getInventoryItems().size());
                    menuMessage = slot.name() + " unequipped.";
                }
                repaint();
                return;
            }
            int index = inventoryMenuRenderer.hitTestBagIndex(point.x, point.y, contentX, contentY - 12,
                    contentWidth, GameMenuRenderer.CONTENT_HEIGHT, playerData.getInventoryItems().size());
            if (index >= 0) {
                if (index == gameMenuController.getInventorySelection() || event.getClickCount() > 1) {
                    playerData.equipItem(index);
                    gameMenuController.setInventorySelection(gameMenuController.getInventorySelection(),
                            playerData.getInventoryItems().size());
                } else {
                    gameMenuController.setInventorySelection(index, playerData.getInventoryItems().size());
                }
                repaint();
            }
        }
    }

    private Point toBasePoint(int screenX, int screenY) {
        double scaleX = (double) getWidth() / BASE_WIDTH;
        double scaleY = (double) getHeight() / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        int offsetX = (int) ((getWidth() - BASE_WIDTH * scale) / 2);
        int offsetY = (int) ((getHeight() - BASE_HEIGHT * scale) / 2);
        int x = (int) ((screenX - offsetX) / scale);
        int y = (int) ((screenY - offsetY) / scale);
        if (x < 0 || x > BASE_WIDTH || y < 0 || y > BASE_HEIGHT) {
            return null;
        }
        return new Point(x, y);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void loadGame(File selectedFile) throws IOException {
        removeKeyListener(this);
        try {
            saveGameService.load(selectedFile, mazeState, skillTreeProgression, playerData);
            skillTreeProgression.reapplyPurchasedStats(playerData);
            screenController.syncFromState();
        } finally {
            addKeyListener(this);
            requestFocusInWindow();
            repaint();
        }
    }
}

