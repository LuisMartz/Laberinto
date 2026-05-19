package game;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainMenu extends JFrame implements GameHost {
    private static final String MAIN_MENU = "mainMenu";
    private static final String MAZE = "maze";
    private static final String COMBAT = "combat";
    private static final String TEMPORARY = "temporary";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final MainMenuPanel mainMenuPanel = new MainMenuPanel();
    private LaberintoPanel laberintoPanel;

    public MainMenu() {
        setTitle("Laberinto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setSize(960, 720);
        setResizable(true);
        setLocationRelativeTo(null);

        ImageIcon gameIcon = AssetLoader.icon("ui/icono_juego.png");
        setIconImage(gameIcon.getImage());

        screens.add(mainMenuPanel, MAIN_MENU);
        add(screens);

        setVisible(true);
    }

    @Override
    public void showMainMenu() {
        cardLayout.show(screens, MAIN_MENU);
        mainMenuPanel.requestFocusInWindow();
    }

    @Override
    public void showMaze(LaberintoPanel panel) {
        laberintoPanel = panel;
        screens.add(laberintoPanel, MAZE);
        cardLayout.show(screens, MAZE);
        revalidate();
        repaint();
        laberintoPanel.requestFocusInWindow();
    }

    @Override
    public void showCombat(PanelTriangulo combatPanel) {
        screens.add(combatPanel, COMBAT);
        cardLayout.show(screens, COMBAT);
        revalidate();
        repaint();
        combatPanel.requestFocusInWindow();
    }

    @Override
    public void showTemporaryScreen(JPanel panel, int delayMs, Runnable afterDelay) {
        screens.add(panel, TEMPORARY);
        cardLayout.show(screens, TEMPORARY);
        revalidate();
        repaint();

        Timer timer = new Timer(delayMs, e -> afterDelay.run());
        timer.setRepeats(false);
        timer.start();
    }

    private void startGame() {
        if (laberintoPanel == null) {
            laberintoPanel = new LaberintoPanel(this);
        }
        showMaze(laberintoPanel);
    }

    private void loadGame(File selectedFile) throws IOException {
        if (laberintoPanel == null) {
            laberintoPanel = new LaberintoPanel(this);
        }
        laberintoPanel.loadGame(selectedFile);
        showMaze(laberintoPanel);
    }

    private void openSettings() {
        String[] options = {"Windowed", "Full Screen"};
        int choice = JOptionPane.showOptionDialog(this,
                "Select Display Mode:", "Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        setFullScreen(choice == 1);
    }

    private void setFullScreen(boolean fullScreen) {
        dispose();
        setUndecorated(fullScreen);
        setExtendedState(fullScreen ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
        setResizable(!fullScreen);
        setVisible(true);
    }

    private final class MainMenuPanel extends JPanel {
        private final Image backgroundImage = AssetLoader.image("backgrounds/background.gif");
        private final JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 12, 0));

        private MainMenuPanel() {
            setLayout(new GridBagLayout());

            buttonPanel.setOpaque(false);
            addMenuButton("ui/start_button.png", e -> startGame());
            addMenuButton("ui/load_button.png", e -> chooseSaveFile());
            addMenuButton("ui/exit_button.png", e -> System.exit(0));
            addMenuButton("ui/boton_opciones.png", e -> openSettings());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.anchor = GridBagConstraints.SOUTH;
            gbc.insets = new Insets(0, 24, 52, 24);
            add(buttonPanel, gbc);
        }

        private void addMenuButton(String iconPath, java.awt.event.ActionListener listener) {
            JButton button = new JButton(AssetLoader.icon(iconPath));
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(listener);
            buttonPanel.add(button);
        }

        private void chooseSaveFile() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
            int result = fileChooser.showOpenDialog(MainMenu.this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                loadGame(fileChooser.getSelectedFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(MainMenu.this, "Could not load save file.", "Load error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}
