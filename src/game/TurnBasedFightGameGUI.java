package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

interface CombatEndListener {
    void onCombatEnd(boolean playerWon);
}

enum MenuState {
    MAIN,
    CATEGORY,
    SKILL_LIST,
    ITEM_LIST
}

class PanelTriangulo extends JPanel {
    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;
    private int trianguloX;
    private int trianguloY;
    private int anchoTriangulo = 20;
    private int altoTriangulo = 20;

    private final String[] mainOptions = {"Attack", "Skills", "Defend", "Items"};
    private String[] currentOptions = mainOptions;
    private int indiceOpcionSeleccionada = 0;

    private MenuState menuState = MenuState.MAIN;
    private SkillCategory currentCategory = SkillCategory.ATTACK;
    private List<Skill> currentSkillList = new ArrayList<>();
    private List<ConsumableStack> currentItemList = new ArrayList<>();

    private BufferedImage imagenFondo;
    private BufferedImage imagenPersonaje;
    private BufferedImage imagenEnemigo;
    private int coordenadaPersonajeX = 150;
    private int coordenadaPersonajeY = 150;
    private int coordenadaEnemigoX = 550;
    private int coordenadaEnemigoY = 30;

    private int coordenadaBarraSaludX = 470;
    private int coordenadaBarraSaludY = 270;
    private int anchoBarraSalud = 200;
    private int altoBarraSalud = 20;
    private int coordenadaBarraManaX = 470;
    private int coordenadaBarraManaY = 300;
    private int anchoBarraMana = 200;
    private int altoBarraMana = 20;

    private int coordenadaBarraSaludEnemigoX = 80;
    private int coordenadaBarraSaludEnemigoY = 20;
    private int anchoBarraSaludEnemigo = 200;
    private int altoBarraSaludEnemigo = 20;
    private int saludEnemigoMaxima = 120;
    private int saludEnemigoActual = 120;
    private int enemyStr = 6;
    private int enemyDef = 4;
    private int enemyAgi = 4;
    private int enemyLuck = 3;

    private boolean playerTurn = true;
    private boolean playerGuarding = false;
    private boolean enemyGuarding = false;
    private boolean combatEnded = false;
    private final Timer enemyTurnTimer;
    private final List<String> combatLog = new ArrayList<>();

    private final PlayerData playerData;
    private CombatEndListener combatEndListener;

    private static final int LOG_LIMIT = 8;
    private static final int XP_REWARD = 40;
    private static final int POTION_HEAL = 30;

    public PanelTriangulo() {
        this(new PlayerData());
    }

    public PanelTriangulo(PlayerData playerData) {
        this.playerData = playerData;
        setPreferredSize(new Dimension(800, 600));
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

        indiceOpcionSeleccionada = 0;
        actualizarPosicionTriangulo();
        logLine("Battle start.");
        logLine("Player turn.");

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!playerTurn || combatEnded) {
                    return;
                }
                int codigoTecla = e.getKeyCode();
                if (codigoTecla == KeyEvent.VK_LEFT && indiceOpcionSeleccionada > 0) {
                    indiceOpcionSeleccionada--;
                } else if (codigoTecla == KeyEvent.VK_RIGHT && indiceOpcionSeleccionada < currentOptions.length - 1) {
                    indiceOpcionSeleccionada++;
                } else if (codigoTecla == KeyEvent.VK_ENTER) {
                    handleSelection();
                } else if (codigoTecla == KeyEvent.VK_ESCAPE) {
                    if (menuState != MenuState.MAIN) {
                        setMenuState(MenuState.MAIN);
                    }
                }
                actualizarPosicionTriangulo();
                repaint();
            }
        });
    }

    private void actualizarPosicionTriangulo() {
        int anchoOpcion = BASE_WIDTH / currentOptions.length;
        trianguloX = anchoOpcion * indiceOpcionSeleccionada + (anchoOpcion / 2) - (anchoTriangulo / 2);
        trianguloY = BASE_HEIGHT - 50;
    }

    private void handleSelection() {
        if (menuState == MenuState.MAIN) {
            handleMainSelection();
        } else if (menuState == MenuState.CATEGORY) {
            handleCategorySelection();
        } else if (menuState == MenuState.SKILL_LIST) {
            handleSkillSelection();
        } else {
            handleItemSelection();
        }
    }

    private void handleMainSelection() {
        String option = currentOptions[indiceOpcionSeleccionada];
        if (option.equals("Attack")) {
            playerGuarding = false;
            performPhysicalAttack("Player", playerData.getTotalStr(), playerData.getTotalAgi(), playerData.getTotalLuck());
            endPlayerTurn();
        } else if (option.equals("Defend")) {
            playerGuarding = true;
            logLine("Player guards.");
            endPlayerTurn();
        } else if (option.equals("Skills")) {
            setMenuState(MenuState.CATEGORY);
        } else if (option.equals("Items")) {
            if (!playerData.hasConsumables()) {
                logLine("No items available.");
                return;
            }
            setMenuState(MenuState.ITEM_LIST);
        }
    }

    private void handleCategorySelection() {
        if (indiceOpcionSeleccionada == currentOptions.length - 1) {
            setMenuState(MenuState.MAIN);
            return;
        }
        currentCategory = SkillCategory.values()[indiceOpcionSeleccionada];
        List<Skill> unlocked = playerData.getSkillTree().getUnlockedSkillsByCategory(currentCategory);
        currentSkillList = new ArrayList<>();
        for (Skill skill : unlocked) {
            if (skill.getType() == SkillType.ACTIVE) {
                currentSkillList.add(skill);
            }
        }
        if (currentSkillList.isEmpty()) {
            logLine("No skills unlocked in this category.");
            setMenuState(MenuState.MAIN);
            return;
        }
        setMenuState(MenuState.SKILL_LIST);
    }

    private void handleSkillSelection() {
        if (indiceOpcionSeleccionada == currentOptions.length - 1) {
            setMenuState(MenuState.CATEGORY);
            return;
        }
        Skill skill = currentSkillList.get(indiceOpcionSeleccionada);
        if (skill.getMpCost() > playerData.getCurrentMp()) {
            logLine("Not enough MP.");
            return;
        }
        playerData.setCurrentMp(playerData.getCurrentMp() - skill.getMpCost());
        executeSkill(skill);
    }

    private void handleItemSelection() {
        if (indiceOpcionSeleccionada == currentOptions.length - 1) {
            setMenuState(MenuState.MAIN);
            return;
        }
        ConsumableStack stack = currentItemList.get(indiceOpcionSeleccionada);
        if (!playerData.useConsumable(stack.getName())) {
            logLine("Item unavailable.");
            setMenuState(MenuState.MAIN);
            return;
        }
        if (stack.getName().equalsIgnoreCase("Potion")) {
            int healAmount = POTION_HEAL + playerData.getTotalDef() / 2;
            playerData.setCurrentHp(playerData.getCurrentHp() + healAmount);
            logLine("Player uses Potion. +" + healAmount + " HP.");
        } else {
            logLine("Player uses " + stack.getName() + ".");
        }
        if (combatEnded) {
            return;
        }
        endPlayerTurn();
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
                logLine("Player heals for " + healAmount + ".");
                break;
            case GUARD:
                playerGuarding = true;
                logLine("Player casts guard.");
                break;
            case RESTORE_MP:
                int mpAmount = skill.getPower() + playerData.getTotalLuck() / 2;
                playerData.setCurrentMp(playerData.getCurrentMp() + mpAmount);
                logLine("Player restores " + mpAmount + " MP.");
                break;
            default:
                logLine("Skill failed.");
                break;
        }

        if (combatEnded) {
            return;
        }
        endPlayerTurn();
    }

    private void performPhysicalAttack(String actor, int str, int agi, int luck) {
        boolean hit = rollHit(agi, enemyAgi, luck, 0);
        if (!hit) {
            logLine(actor + " missed.");
            return;
        }
        boolean crit = rollCrit(luck);
        int damage = calculateDamage(str, enemyDef, enemyGuarding, crit);
        applyEnemyDamage(damage);
        logLine(actor + (crit ? " crits" : " hits") + " for " + damage + ".");
        enemyGuarding = false;
        checkCombatEnd();
    }

    private void performSkillPhysical(Skill skill) {
        boolean hit = rollHit(playerData.getTotalAgi(), enemyAgi, playerData.getTotalLuck(), skill.getAccuracyBonus());
        if (!hit) {
            logLine(skill.getName() + " missed.");
            return;
        }
        boolean crit = rollCrit(playerData.getTotalLuck());
        int damage = calculateDamage(playerData.getTotalStr(), enemyDef, enemyGuarding, crit) + skill.getPower();
        applyEnemyDamage(damage);
        logLine(skill.getName() + (crit ? " crits" : " hits") + " for " + damage + ".");
        enemyGuarding = false;
        checkCombatEnd();
    }

    private void performSkillMagic(Skill skill) {
        boolean crit = rollCrit(playerData.getTotalLuck());
        int base = skill.getPower() + playerData.getTotalLuck();
        int damage = Math.max(1, base - enemyDef / 2);
        if (crit) {
            damage = (int) Math.round(damage * 1.5);
        }
        if (enemyGuarding) {
            damage = Math.max(1, damage / 2);
        }
        applyEnemyDamage(damage);
        logLine(skill.getName() + (crit ? " crits" : " hits") + " for " + damage + ".");
        enemyGuarding = false;
        checkCombatEnd();
    }

    private void endPlayerTurn() {
        if (combatEnded) {
            return;
        }
        playerTurn = false;
        setMenuState(MenuState.MAIN);
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
            logLine("Enemy guards.");
        } else {
            performEnemyAttack();
        }
        if (combatEnded) {
            return;
        }
        playerTurn = true;
        logLine("Player turn.");
        repaint();
    }

    private void performEnemyAttack() {
        int guardAccuracyPenalty = playerGuarding ? -15 : 0;
        boolean hit = rollHit(enemyAgi, playerData.getTotalAgi(), enemyLuck, guardAccuracyPenalty);
        if (!hit) {
            logLine("Enemy missed.");
            return;
        }
        boolean crit = rollCrit(enemyLuck);
        int damage = calculateDamage(enemyStr, playerData.getTotalDef(), playerGuarding, crit);
        playerData.setCurrentHp(playerData.getCurrentHp() - damage);
        logLine("Enemy" + (crit ? " crits" : " hits") + " for " + damage + ".");
        playerGuarding = false;
        checkCombatEnd();
    }

    private void setMenuState(MenuState state) {
        menuState = state;
        indiceOpcionSeleccionada = 0;
        if (state == MenuState.MAIN) {
            currentOptions = mainOptions;
        } else if (state == MenuState.CATEGORY) {
            SkillCategory[] categories = SkillCategory.values();
            String[] options = new String[categories.length + 1];
            for (int i = 0; i < categories.length; i++) {
                options[i] = formatCategory(categories[i]);
            }
            options[categories.length] = "Back";
            currentOptions = options;
        } else if (state == MenuState.SKILL_LIST) {
            String[] options = new String[currentSkillList.size() + 1];
            for (int i = 0; i < currentSkillList.size(); i++) {
                Skill skill = currentSkillList.get(i);
                options[i] = skill.getName() + " (MP " + skill.getMpCost() + ")";
            }
            options[currentSkillList.size()] = "Back";
            currentOptions = options;
        } else {
            currentItemList = playerData.getConsumables();
            String[] options = new String[currentItemList.size() + 1];
            for (int i = 0; i < currentItemList.size(); i++) {
                ConsumableStack stack = currentItemList.get(i);
                options[i] = stack.getName() + " x" + stack.getCount();
            }
            options[currentItemList.size()] = "Back";
            currentOptions = options;
        }
        actualizarPosicionTriangulo();
    }

    private String formatCategory(SkillCategory category) {
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

    private boolean rollHit(int attackerAgi, int defenderAgi, int attackerLuck, int accuracyBonus) {
        double chance = 0.75 + (attackerAgi - defenderAgi) * 0.03 + attackerLuck * 0.01 + accuracyBonus * 0.01;
        chance = Math.max(0.1, Math.min(0.95, chance));
        return Math.random() < chance;
    }

    private boolean rollCrit(int attackerLuck) {
        double chance = 0.05 + attackerLuck * 0.01;
        chance = Math.max(0.05, Math.min(0.25, chance));
        return Math.random() < chance;
    }

    private int calculateDamage(int attackerStr, int defenderDef, boolean defenderGuarding, boolean crit) {
        int base = 5 + attackerStr * 2;
        int damage = base - defenderDef;
        if (damage < 1) {
            damage = 1;
        }
        if (crit) {
            damage = (int) Math.round(damage * 1.5);
        }
        if (defenderGuarding) {
            damage = Math.max(1, damage / 2);
        }
        return damage;
    }

    private void applyEnemyDamage(int damage) {
        saludEnemigoActual = Math.max(0, saludEnemigoActual - damage);
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
        if (saludEnemigoActual <= 0 || playerData.getCurrentHp() <= 0) {
            combatEnded = true;
            boolean playerWon = saludEnemigoActual <= 0 && playerData.getCurrentHp() > 0;
            if (playerWon) {
                logLine("Victory! XP +" + XP_REWARD + ".");
                boolean leveled = playerData.addExperience(XP_REWARD);
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
        g2.drawRect(coordenadaBarraSaludEnemigoX, coordenadaBarraSaludEnemigoY, anchoBarraSaludEnemigo, altoBarraSaludEnemigo);
        g2.setColor(Color.RED);
        int anchoRellenoBarraSaludEnemigo = (int) ((double) saludEnemigoActual / saludEnemigoMaxima * anchoBarraSaludEnemigo);
        g2.fillRect(coordenadaBarraSaludEnemigoX, coordenadaBarraSaludEnemigoY, anchoRellenoBarraSaludEnemigo, altoBarraSaludEnemigo);

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

    private void drawOptions(Graphics g) {
        FontMetrics fontMetrics = g.getFontMetrics();
        int anchoOpcion = BASE_WIDTH / currentOptions.length;
        for (int i = 0; i < currentOptions.length; i++) {
            g.setColor(getOptionColor(i));
            int stringX = i * anchoOpcion + (anchoOpcion - fontMetrics.stringWidth(currentOptions[i])) / 2;
            int stringY = BASE_HEIGHT - 10;
            g.drawString(currentOptions[i], stringX, stringY);
        }

        g.setColor(Color.RED);
        int[] xPoints = {trianguloX, trianguloX + anchoTriangulo / 2, trianguloX + anchoTriangulo};
        int[] yPoints = {trianguloY, trianguloY + altoTriangulo, trianguloY};
        g.fillPolygon(xPoints, yPoints, 3);
    }

    private Color getOptionColor(int optionIndex) {
        if (menuState != MenuState.MAIN) {
            return Color.WHITE;
        }
        String option = currentOptions[optionIndex];
        if (option.equals("Skills")) {
            return hasUsableSkills() ? Color.WHITE : Color.GRAY;
        }
        if (option.equals("Items")) {
            return playerData.hasConsumables() ? Color.WHITE : Color.GRAY;
        }
        return Color.WHITE;
    }

    private boolean hasUsableSkills() {
        for (SkillCategory category : SkillCategory.values()) {
            List<Skill> unlocked = playerData.getSkillTree().getUnlockedSkillsByCategory(category);
            for (Skill skill : unlocked) {
                if (skill.getType() == SkillType.ACTIVE) {
                    return true;
                }
            }
        }
        return false;
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
        return new Dimension(800, 600);
    }
}

public class TurnBasedFightGameGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Selector de Triangulo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new PanelTriangulo());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
