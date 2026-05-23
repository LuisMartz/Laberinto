package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CombatState {
    private final List<Combatant> combatants;
    private final TurnManager turnManager;
    private boolean ended;

    public CombatState(Combatant player, Combatant enemy) {
        this(Arrays.asList(player, enemy));
    }

    public CombatState(List<Combatant> combatants) {
        this.combatants = new ArrayList<>(combatants);
        this.turnManager = new TurnManager(this.combatants);
    }

    public Combatant getPlayer() {
        return getAllies().isEmpty() ? null : getAllies().get(0);
    }

    public Combatant getEnemy() {
        return getFirstAliveEnemy();
    }

    public List<Combatant> getCombatants() {
        return Collections.unmodifiableList(combatants);
    }

    public List<Combatant> getAllies() {
        List<Combatant> allies = new ArrayList<>();
        for (Combatant combatant : combatants) {
            if (combatant.isPlayerControlled()) {
                allies.add(combatant);
            }
        }
        return allies;
    }

    public List<Combatant> getEnemies() {
        List<Combatant> enemies = new ArrayList<>();
        for (Combatant combatant : combatants) {
            if (!combatant.isPlayerControlled()) {
                enemies.add(combatant);
            }
        }
        return enemies;
    }

    public Combatant getFirstAliveEnemy() {
        for (Combatant enemy : getEnemies()) {
            if (enemy.isAlive()) {
                return enemy;
            }
        }
        return null;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public Combatant getCurrentTurn() {
        return turnManager.getCurrentTurn();
    }

    public void finishTurn(Combatant combatant) {
        turnManager.advanceAfterTurn(combatant);
    }

    public void finishTurn(Combatant combatant, CombatAction action) {
        turnManager.advanceAfterTurn(combatant, action == null ? 0 : action.getDelayModifier());
    }

    public boolean isEnded() {
        return ended || areAllPlayersDefeated() || areAllEnemiesDefeated();
    }

    public void markEnded() {
        ended = true;
    }

    public boolean didPlayerWin() {
        return areAllEnemiesDefeated() && !areAllPlayersDefeated();
    }

    public boolean areAllEnemiesDefeated() {
        List<Combatant> enemies = getEnemies();
        if (enemies.isEmpty()) {
            return true;
        }
        for (Combatant enemy : enemies) {
            if (enemy.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public boolean areAllPlayersDefeated() {
        List<Combatant> allies = getAllies();
        if (allies.isEmpty()) {
            return true;
        }
        for (Combatant ally : allies) {
            if (ally.isAlive()) {
                return false;
            }
        }
        return true;
    }
}
