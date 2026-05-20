package game;

import java.util.Random;

class MazeScreenController {
    interface View {
        void repaintMaze();

        void pauseEnemyMovement();

        void resumeEnemyMovement();

        boolean showCombatPanel(PanelTriangulo combatPanel);

        void showMazePanel();

        void showLevelLoading(String message, Runnable onComplete);

        void showMessage(String message);

        void setMenuMessage(String message);
    }

    private final MazeState mazeState;
    private final PlayerData playerData;
    private final View view;
    private final LootFactory lootFactory = new LootFactory();
    private final MazeController mazeController;
    private CombatEnemy pendingCombatEnemy;
    private boolean fightingGameStarted;
    private boolean inCombat;
    private int currentLevel;

    MazeScreenController(MazeState mazeState, PlayerData playerData, View view) {
        this.mazeState = mazeState;
        this.playerData = playerData;
        this.view = view;
        this.currentLevel = mazeState.getLevel();
        this.mazeController = new MazeController(mazeState, new MazeController.Listener() {
            @Override
            public void onCombatRequested(CombatEnemy enemy) {
                startCombat(enemy);
            }

            @Override
            public void onLevelExitRequested(int nextLevel) {
                currentLevel = nextLevel;
                loadNextMaze();
            }

            @Override
            public void onMazeChanged() {
                view.repaintMaze();
            }
        });
    }

    boolean isInCombat() {
        return inCombat;
    }

    void handleMazeKey(int key) {
        mazeController.handleKey(key);
        syncFromState();
        view.repaintMaze();
    }

    void moveEnemy() {
        if (!inCombat) {
            mazeController.moveEnemy();
            view.repaintMaze();
        }
    }

    void syncFromState() {
        currentLevel = mazeState.getLevel();
    }

    private void startCombat(CombatEnemy combatEnemy) {
        if (fightingGameStarted || inCombat) {
            return;
        }
        pendingCombatEnemy = combatEnemy;
        mazeState.savePlayerPosition();
        inCombat = true;
        fightingGameStarted = true;
        view.pauseEnemyMovement();

        PanelTriangulo combatPanel = new PanelTriangulo(playerData, pendingCombatEnemy);
        combatPanel.setCombatEndListener(playerWon -> {
            resumeFromCombat(playerWon);
            view.showMazePanel();
        });

        if (!view.showCombatPanel(combatPanel)) {
            inCombat = false;
            fightingGameStarted = false;
            view.resumeEnemyMovement();
        }
    }

    private void resumeFromCombat(boolean playerWon) {
        inCombat = false;
        fightingGameStarted = false;
        mazeState.restorePlayerPosition();
        if (playerWon) {
            mazeState.defeatEnemy();
            applyCombatLoot();
        } else {
            repositionEnemyAwayFromPlayer();
        }
        syncFromState();
        view.resumeEnemyMovement();
        if (!playerWon) {
            view.showMessage("You lost the fight. Returning to the maze.");
        }
    }

    private void loadNextMaze() {
        view.pauseEnemyMovement();
        String message = getLevelLoadingMessage();
        view.showLevelLoading(message, this::finishLoadingNextMaze);
    }

    private String getLevelLoadingMessage() {
        if (currentLevel <= 5) {
            return "Mundo 1 - Nivel " + currentLevel;
        }
        return "Mundo 2 - Nivel " + (currentLevel - 5);
    }

    private void finishLoadingNextMaze() {
        mazeState.generateLevel(currentLevel);
        syncFromState();
        fightingGameStarted = false;
        view.resumeEnemyMovement();
        view.repaintMaze();
    }

    private void applyCombatLoot() {
        CombatEnemy enemy = pendingCombatEnemy == null
                ? CombatEnemy.forLevel(mazeState.getLevel(), false)
                : pendingCombatEnemy;
        LootDrop drop = lootFactory.rollForEnemy(enemy, mazeState.getLevel(), false);
        mazeState.addScore(drop.getCoins());
        StringBuilder message = new StringBuilder("Loot: +").append(drop.getCoins()).append(" coins");
        if (drop.hasItem()) {
            playerData.addItem(drop.getItem());
            message.append(", ").append(drop.getRarity().getLabel()).append(" item: ").append(drop.getItem().getName());
        }
        view.setMenuMessage(message.toString());
        view.showMessage(message.toString());
    }

    private void repositionEnemyAwayFromPlayer() {
        Random random = new Random();
        int x;
        int y;
        do {
            x = random.nextInt(mazeState.getRows());
            y = random.nextInt(mazeState.getColumns());
        } while (mazeState.getTile(x, y) != TileType.FLOOR.getCode()
                || Math.abs(x - mazeState.getPlayerRow()) + Math.abs(y - mazeState.getPlayerColumn()) < 4);
        mazeState.moveEnemyTo(x, y);
    }
}
