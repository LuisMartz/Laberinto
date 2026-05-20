package game;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PanelTriangulo extends JPanel {
    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private static final int LOG_LIMIT = 8;
    private static final int POTION_HEAL = 30;

    private int trianguloX;
    private int trianguloY;
    private final int anchoTriangulo = 20;
    private final int altoTriangulo = 20;

    private final CombatMenuController combatMenu;

    private BufferedImage imagenFondo;
    private BufferedImage imagenPersonaje;
    private BufferedImage imagenEnemigo;
    private final int coordenadaPersonajeX = 150;
    private final int coordenadaPersonajeY = 150;
    private final int coordenadaEnemigoX = 550;
    private final int coordenadaEnemigoY = 30;

    private final int coordenadaBarraSaludX = 470;
    private final int coordenadaBarraSaludY = 270;
    private final int anchoBarraSalud = 200;
    private final int altoBarraSalud = 20;
    private final int coordenadaBarraManaX = 470;
    private final int coordenadaBarraManaY = 300;
    private final int anchoBarraMana = 200;
    private final int altoBarraMana = 20;

    private final int coordenadaBarraSaludEnemigoX = 80;
    private final int coordenadaBarraSaludEnemigoY = 20;
    private final int anchoBarraSaludEnemigo = 200;
    private final int altoBarraSaludEnemigo = 20;
    private final int saludEnemigoMaxima;
    private int saludEnemigoActual;
    private final int xpReward;
    private final String enemyName;
    private final CombatState combatState;
    private final CombatResolver combatResolver;

    private boolean playerTurn = true;
    private boolean playerGuarding = false;
    private boolean enemyGuarding = false;
    private boolean combatEnded = false;
    private final Timer enemyTurnTimer;
    private final List<String> combatLog = new ArrayList<>();

    private final PlayerData playerData;
    private CombatEndListener combatEndListener;

    public PanelTriangulo() {
        this(new PlayerData(), CombatEnemy.forLevel(1, false));
    }

    public PanelTriangulo(PlayerData playerData) {
        this(playerData, CombatEnemy.forLevel(1, false));
    }

    public PanelTriangulo(PlayerData playerData, CombatEnemy combatEnemy) {
        this.playerData = playerData;
        this.enemyName = combatEnemy.getName();
        this.saludEnemigoMaxima = combatEnemy.getMaxHp();
        this.saludEnemigoActual = combatEnemy.getMaxHp();
        this.xpReward = combatEnemy.getXpReward();
        this.combatState = new CombatState(Combatant.fromPlayerData(playerData), Combatant.fromEnemy(combatEnemy));
        this.combatResolver = new CombatResolver();
        this.combatMenu = new CombatMenuController(playerData);
        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);

        try {
            imagenFondo = AssetLoader.imageBuffer("backgrounds/combat_background.png");
            imagenPersonaje = AssetLoader.imageBuffer("player/larry (2).png");
            imagenEnemigo = AssetLoader.imageBuffer("enemies/Enemigo.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        enemyTurnTimer = new Timer(700, e -> executeEnemyTurn());
        enemyTurnTimer.setRepeats(false);

        actualizarPosicionTriangulo();
        logLine(enemyName + " appears.");
        logLine("Player turn.");

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!playerTurn || combatEnded) {
                    return;
                }
                int codigoTecla = e.getKeyCode();
                if (codigoTecla == KeyEvent.VK_LEFT) {
                    combatMenu.moveLeft();
                } else if (codigoTecla == KeyEvent.VK_RIGHT) {
                    combatMenu.moveRight();
                } else if (codigoTecla == KeyEvent.VK_ENTER) {
                    if (combatMenu.isCurrentOptionEnabled()) {
                        handleSelection();
                    } else {
                        logLine("No option available.");
                    }
                } else if (codigoTecla == KeyEvent.VK_ESCAPE && combatMenu.getState() != MenuState.MAIN) {
                    combatMenu.showMain();
                }
                actualizarPosicionTriangulo();
                repaint();
            }
        });
    }

    private void actualizarPosicionTriangulo() {
        int anchoOpcion = BASE_WIDTH / combatMenu.getOptions().length;
        trianguloX = anchoOpcion * combatMenu.getSelectedIndex() + (anchoOpcion / 2) - (anchoTriangulo / 2);
        trianguloY = BASE_HEIGHT - 50;
    }

    private void handleSelection() {
        if (combatMenu.getState() == MenuState.MAIN) {
            handleMainSelection();
        } else if (combatMenu.getState() == MenuState.CATEGORY) {
            handleCategorySelection();
        } else if (combatMenu.getState() == MenuState.SKILL_LIST) {
            handleSkillSelection();
        } else {
            handleItemSelection();
        }
    }

    private void handleMainSelection() {
        String option = combatMenu.getSelectedOption();
        if (option.equals("Attack")) {
            playerGuarding = false;
            performPhysicalAttack();
            endPlayerTurn();
        } else if (option.equals("Defend")) {
            playerGuarding = true;
            combatState.getPlayer().setGuarding(true);
            logLine("Player guards.");
            endPlayerTurn();
        } else if (option.equals("Skills")) {
            if (!combatMenu.hasUsableSkills()) {
                logLine("No skills available.");
                return;
            }
            combatMenu.showCategories();
        } else if (option.equals("Items")) {
            if (!playerData.hasConsumables()) {
                logLine("No items available.");
                return;
            }
            combatMenu.showItems();
        }
    }

    private void handleCategorySelection() {
        if (combatMenu.isBackSelected()) {
            combatMenu.showMain();
            return;
        }
        if (!combatMenu.chooseCategory()) {
            logLine("No active skills in this category.");
            return;
        }
    }

    private void handleSkillSelection() {
        if (combatMenu.isBackSelected()) {
            combatMenu.showCategories();
            return;
        }
        Skill skill = combatMenu.getSelectedSkill();
        if (skill == null) {
            logLine("Skill unavailable.");
            combatMenu.showMain();
            return;
        }
        if (skill.getMpCost() > playerData.getCurrentMp()) {
            logLine("Not enough MP.");
            return;
        }
        playerData.setCurrentMp(playerData.getCurrentMp() - skill.getMpCost());
        combatState.getPlayer().spendMp(skill.getMpCost());
        executeSkill(skill);
    }

    private void handleItemSelection() {
        if (combatMenu.isBackSelected()) {
            combatMenu.showMain();
            return;
        }
        ConsumableStack stack = combatMenu.getSelectedItem();
        if (stack == null) {
            logLine("Item unavailable.");
            combatMenu.showMain();
            return;
        }
        if (!playerData.useConsumable(stack.getName())) {
            logLine("Item unavailable.");
            combatMenu.showMain();
            return;
        }
        if (stack.getName().equalsIgnoreCase("Potion")) {
            int healAmount = POTION_HEAL + playerData.getTotalDef() / 2;
            playerData.setCurrentHp(playerData.getCurrentHp() + healAmount);
            combatState.getPlayer().heal(healAmount);
            logLine("Player uses Potion. +" + healAmount + " HP.");
        } else {
            logLine("Player uses " + stack.getName() + ".");
        }
        if (!combatEnded) {
            endPlayerTurn();
        }
    }

    private void executeSkill(Skill skill) {
        playerGuarding = false;
        switch (skill.getAction()) {
            case PHYSICAL:
                performSkillPhysical(skill);
                break;
            case MAGIC:
                performSkillMagic(skill);
                break;
            case HEAL:
                int healAmount = skill.getPower() + playerData.getTotalDef();
                playerData.setCurrentHp(playerData.getCurrentHp() + healAmount);
                combatState.getPlayer().heal(healAmount);
                logLine("Player heals for " + healAmount + ".");
                break;
            case GUARD:
                playerGuarding = true;
                combatState.getPlayer().setGuarding(true);
                logLine("Player casts guard.");
                break;
            case RESTORE_MP:
                int mpAmount = skill.getPower() + playerData.getTotalLuck() / 2;
                playerData.setCurrentMp(playerData.getCurrentMp() + mpAmount);
                combatState.getPlayer().restoreMp(mpAmount);
                logLine("Player restores " + mpAmount + " MP.");
                break;
            default:
                logLine("Skill failed.");
                break;
        }

        if (!combatEnded) {
            endPlayerTurn();
        }
    }

    private void performPhysicalAttack() {
        combatState.getEnemy().setGuarding(enemyGuarding);
        CombatResult result = combatResolver.resolvePhysicalAttack(combatState.getPlayer(), combatState.getEnemy(), 0, 0);
        saludEnemigoActual = combatState.getEnemy().getCurrentHp();
        logLine(result.getMessage());
        enemyGuarding = combatState.getEnemy().isGuarding();
        checkCombatEnd();
    }

    private void performSkillPhysical(Skill skill) {
        combatState.getEnemy().setGuarding(enemyGuarding);
        CombatResult result = combatResolver.resolvePhysicalAttack(combatState.getPlayer(), combatState.getEnemy(),
                skill.getAccuracyBonus(), skill.getPower());
        saludEnemigoActual = combatState.getEnemy().getCurrentHp();
        logLine(result.getMessage().replace(combatState.getPlayer().getName(), skill.getName()));
        enemyGuarding = combatState.getEnemy().isGuarding();
        checkCombatEnd();
    }

    private void performSkillMagic(Skill skill) {
        combatState.getEnemy().setGuarding(enemyGuarding);
        CombatResult result = combatResolver.resolveMagicAttack(combatState.getPlayer(), combatState.getEnemy(), skill);
        saludEnemigoActual = combatState.getEnemy().getCurrentHp();
        logLine(result.getMessage());
        enemyGuarding = combatState.getEnemy().isGuarding();
        checkCombatEnd();
    }

    private void endPlayerTurn() {
        if (combatEnded) {
            return;
        }
        combatState.finishTurn(combatState.getPlayer());
        playerTurn = false;
        combatMenu.showMain();
        logLine("Enemy turn...");
        enemyTurnTimer.restart();
    }

    private void executeEnemyTurn() {
        if (combatEnded) {
            return;
        }
        double hpRatio = (double) saludEnemigoActual / Math.max(1, saludEnemigoMaxima);
        if (hpRatio < 0.3 && Math.random() < 0.4) {
            enemyGuarding = true;
            combatState.getEnemy().setGuarding(true);
            logLine("Enemy guards.");
        } else {
            performEnemyAttack();
        }
        if (combatEnded) {
            return;
        }
        combatState.finishTurn(combatState.getEnemy());
        playerTurn = true;
        logLine("Player turn.");
        repaint();
    }

    private void performEnemyAttack() {
        int guardAccuracyPenalty = playerGuarding ? -15 : 0;
        combatState.getPlayer().setGuarding(playerGuarding);
        CombatResult result = combatResolver.resolvePhysicalAttack(combatState.getEnemy(), combatState.getPlayer(),
                guardAccuracyPenalty, 0);
        playerData.setCurrentHp(combatState.getPlayer().getCurrentHp());
        logLine(result.getMessage());
        playerGuarding = combatState.getPlayer().isGuarding();
        checkCombatEnd();
    }

    private void logLine(String message) {
        combatLog.add(message);
        while (combatLog.size() > LOG_LIMIT) {
            combatLog.remove(0);
        }
    }

    public void setCombatEndListener(CombatEndListener combatEndListener) {
        this.combatEndListener = combatEndListener;
    }

    private void checkCombatEnd() {
        if (combatEnded) {
            return;
        }
        if (combatState.isEnded()) {
            combatState.markEnded();
            combatEnded = true;
            boolean playerWon = combatState.didPlayerWin();
            if (playerWon) {
                logLine("Victory! XP +" + xpReward + ".");
                boolean leveled = playerData.addExperience(xpReward);
                if (leveled) {
                    logLine("Level up! Level " + playerData.getLevel() + ".");
                }
            } else {
                logLine("Defeat...");
            }
            if (combatEndListener != null) {
                combatEndListener.onCombatEnd(playerWon);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        double scaleX = (double) getWidth() / BASE_WIDTH;
        double scaleY = (double) getHeight() / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        int offsetX = (int) ((getWidth() - BASE_WIDTH * scale) / 2);
        int offsetY = (int) ((getHeight() - BASE_HEIGHT * scale) / 2);
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        if (imagenFondo != null) {
            g2.drawImage(imagenFondo, 0, 0, BASE_WIDTH, BASE_HEIGHT, this);
        }

        drawHealthBars(g2);

        if (imagenPersonaje != null) {
            g2.drawImage(imagenPersonaje, coordenadaPersonajeX, coordenadaPersonajeY, null);
        }
        if (imagenEnemigo != null) {
            g2.drawImage(imagenEnemigo, coordenadaEnemigoX, coordenadaEnemigoY, null);
        }

        drawOptions(g2);
        drawLog(g2);
        g2.dispose();
    }

    private void drawHealthBars(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.drawRect(coordenadaBarraSaludX, coordenadaBarraSaludY, anchoBarraSalud, altoBarraSalud);
        g2.setColor(Color.RED);
        int maxHp = Math.max(1, playerData.getMaxHp());
        int anchoRellenoBarraSalud = (int) ((double) playerData.getCurrentHp() / maxHp * anchoBarraSalud);
        g2.fillRect(coordenadaBarraSaludX, coordenadaBarraSaludY, anchoRellenoBarraSalud, altoBarraSalud);

        g2.setColor(Color.WHITE);
        g2.drawRect(coordenadaBarraManaX, coordenadaBarraManaY, anchoBarraMana, altoBarraMana);
        g2.setColor(Color.BLUE);
        int maxMp = Math.max(1, playerData.getMaxMp());
        int anchoRellenoBarraMana = (int) ((double) playerData.getCurrentMp() / maxMp * anchoBarraMana);
        g2.fillRect(coordenadaBarraManaX, coordenadaBarraManaY, anchoRellenoBarraMana, altoBarraMana);

        g2.setColor(Color.WHITE);
        g2.drawRect(coordenadaBarraSaludEnemigoX, coordenadaBarraSaludEnemigoY, anchoBarraSaludEnemigo,
                altoBarraSaludEnemigo);
        g2.drawString(enemyName, coordenadaBarraSaludEnemigoX, coordenadaBarraSaludEnemigoY + 40);
        g2.setColor(Color.RED);
        int anchoRellenoBarraSaludEnemigo = (int) ((double) saludEnemigoActual / saludEnemigoMaxima
                * anchoBarraSaludEnemigo);
        g2.fillRect(coordenadaBarraSaludEnemigoX, coordenadaBarraSaludEnemigoY, anchoRellenoBarraSaludEnemigo,
                altoBarraSaludEnemigo);
    }

    private void drawOptions(Graphics g) {
        FontMetrics fontMetrics = g.getFontMetrics();
        String[] options = combatMenu.getOptions();
        int anchoOpcion = BASE_WIDTH / options.length;
        for (int i = 0; i < options.length; i++) {
            g.setColor(getOptionColor(i));
            int stringX = i * anchoOpcion + (anchoOpcion - fontMetrics.stringWidth(options[i])) / 2;
            int stringY = BASE_HEIGHT - 10;
            g.drawString(options[i], stringX, stringY);
        }

        g.setColor(Color.RED);
        int[] xPoints = {trianguloX, trianguloX + anchoTriangulo / 2, trianguloX + anchoTriangulo};
        int[] yPoints = {trianguloY, trianguloY + altoTriangulo, trianguloY};
        g.fillPolygon(xPoints, yPoints, 3);
    }

    private Color getOptionColor(int optionIndex) {
        return combatMenu.isOptionEnabled(optionIndex) ? Color.WHITE : Color.GRAY;
    }

    private void drawLog(Graphics g) {
        int boxX = 20;
        int boxY = 340;
        int boxW = 360;
        int boxH = 160;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(Color.WHITE);
        g.drawRect(boxX, boxY, boxW, boxH);

        int lineY = boxY + 20;
        for (String line : combatLog) {
            g.drawString(line, boxX + 10, lineY);
            lineY += 18;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BASE_WIDTH, BASE_HEIGHT);
    }
}
