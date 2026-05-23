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
    private final int xpReward;
    private final String enemyName;
    private final CombatState combatState;
    private final CombatResolver combatResolver;

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

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Combatant active = combatState.getCurrentTurn();
                if (combatEnded || active == null || !active.isPlayerControlled()) {
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
        advanceCombatFlow();
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
            performPhysicalAttack();
            endActiveTurn(CombatAction.attack());
        } else if (option.equals("Defend")) {
            Combatant active = combatState.getCurrentTurn();
            if (active != null) {
                active.setGuarding(true);
            }
            logLine("Player guards.");
            endActiveTurn(CombatAction.defend());
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
        Combatant active = combatState.getCurrentTurn();
        if (active != null) {
            active.spendMp(skill.getMpCost());
        }
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
            Combatant active = combatState.getCurrentTurn();
            if (active != null) {
                active.heal(healAmount);
            }
            logLine("Player uses Potion. +" + healAmount + " HP.");
        } else {
            logLine("Player uses " + stack.getName() + ".");
        }
        if (!combatEnded) {
            endActiveTurn(CombatAction.item());
        }
    }

    private void executeSkill(Skill skill) {
        Combatant active = combatState.getCurrentTurn();
        if (active != null) {
            active.setGuarding(false);
        }
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
                if (active != null) {
                    active.heal(healAmount);
                }
                logLine("Player heals for " + healAmount + ".");
                break;
            case GUARD:
                if (active != null) {
                    active.setGuarding(true);
                }
                logLine("Player casts guard.");
                break;
            case RESTORE_MP:
                int mpAmount = skill.getPower() + playerData.getTotalLuck() / 2;
                playerData.setCurrentMp(playerData.getCurrentMp() + mpAmount);
                if (active != null) {
                    active.restoreMp(mpAmount);
                }
                logLine("Player restores " + mpAmount + " MP.");
                break;
            default:
                logLine("Skill failed.");
                break;
        }

        if (!combatEnded) {
            endActiveTurn(CombatAction.skill(skill));
        }
    }

    private void performPhysicalAttack() {
        Combatant attacker = combatState.getCurrentTurn();
        Combatant defender = combatState.getFirstAliveEnemy();
        if (attacker == null || defender == null) {
            return;
        }
        CombatResult result = combatResolver.resolvePhysicalAttack(attacker, defender, 0, 0);
        logLine(result.getMessage());
        checkCombatEnd();
    }

    private void performSkillPhysical(Skill skill) {
        Combatant attacker = combatState.getCurrentTurn();
        Combatant defender = combatState.getFirstAliveEnemy();
        if (attacker == null || defender == null) {
            return;
        }
        CombatResult result = combatResolver.resolvePhysicalAttack(attacker, defender,
                skill.getAccuracyBonus(), skill.getPower());
        logLine(result.getMessage().replace(attacker.getName(), skill.getName()));
        checkCombatEnd();
    }

    private void performSkillMagic(Skill skill) {
        Combatant attacker = combatState.getCurrentTurn();
        Combatant defender = combatState.getFirstAliveEnemy();
        if (attacker == null || defender == null) {
            return;
        }
        CombatResult result = combatResolver.resolveMagicAttack(attacker, defender, skill);
        logLine(result.getMessage());
        checkCombatEnd();
    }

    private void endActiveTurn(CombatAction action) {
        if (combatEnded) {
            return;
        }
        combatState.finishTurn(combatState.getCurrentTurn(), action);
        combatMenu.showMain();
        advanceCombatFlow();
    }

    private void advanceCombatFlow() {
        if (combatEnded || combatState.isEnded()) {
            checkCombatEnd();
            return;
        }
        Combatant active = combatState.getCurrentTurn();
        if (active == null) {
            checkCombatEnd();
            return;
        }
        announceCurrentTurn();
        if (!active.isPlayerControlled()) {
            enemyTurnTimer.restart();
        }
    }

    private void announceCurrentTurn() {
        Combatant active = combatState.getCurrentTurn();
        if (active != null) {
            logLine(active.getName() + " turn.");
        }
    }

    private void executeEnemyTurn() {
        if (combatEnded) {
            return;
        }
        Combatant active = combatState.getCurrentTurn();
        if (active == null || active.isPlayerControlled()) {
            return;
        }
        double hpRatio = (double) active.getCurrentHp() / Math.max(1, active.getMaxHp());
        if (hpRatio < 0.3 && Math.random() < 0.4) {
            active.setGuarding(true);
            logLine(active.getName() + " guards.");
            combatState.finishTurn(active, CombatAction.defend());
        } else {
            performEnemyAttack();
            combatState.finishTurn(active, CombatAction.attack());
        }
        advanceCombatFlow();
        repaint();
    }

    private void performEnemyAttack() {
        Combatant attacker = combatState.getCurrentTurn();
        Combatant defender = firstAlivePlayer();
        if (attacker == null || defender == null) {
            return;
        }
        int guardAccuracyPenalty = defender.isGuarding() ? -15 : 0;
        CombatResult result = combatResolver.resolvePhysicalAttack(attacker, defender,
                guardAccuracyPenalty, 0);
        if (defender.isPlayerControlled()) {
            playerData.setCurrentHp(defender.getCurrentHp());
        }
        logLine(result.getMessage());
        checkCombatEnd();
    }

    private Combatant firstAlivePlayer() {
        for (Combatant ally : combatState.getAllies()) {
            if (ally.isAlive()) {
                return ally;
            }
        }
        return null;
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
        Combatant enemy = combatState.getFirstAliveEnemy();
        int enemyHp = enemy == null ? 0 : enemy.getCurrentHp();
        int enemyMaxHp = enemy == null ? 1 : enemy.getMaxHp();
        int anchoRellenoBarraSaludEnemigo = (int) ((double) enemyHp / enemyMaxHp * anchoBarraSaludEnemigo);
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
