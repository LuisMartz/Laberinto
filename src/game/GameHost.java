package game;

import javax.swing.JPanel;

public interface GameHost {
    void showMainMenu();

    void showMaze(LaberintoPanel panel);

    void showCombat(PanelTriangulo combatPanel);

    void showTemporaryScreen(JPanel panel, int delayMs, Runnable afterDelay);
}
