package game;

import java.util.Arrays;

public class CombatState {
    private final Combatant player;
    private final Combatant enemy;
    private final TurnManager turnManager;
    private boolean ended;

    public CombatState(Combatant player, Combatant enemy) {
        this.player = player;
        this.enemy = enemy;
        this.turnManager = new TurnManager(Arrays.asList(player, enemy));
    }

    public Combatant getPlayer() {
        return player;
    }

    public Combatant getEnemy() {
        return enemy;
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

    public boolean isEnded() {
        return ended || !player.isAlive() || !enemy.isAlive();
    }

    public void markEnded() {
        ended = true;
    }

    public boolean didPlayerWin() {
        return enemy.getCurrentHp() <= 0 && player.getCurrentHp() > 0;
    }
}
