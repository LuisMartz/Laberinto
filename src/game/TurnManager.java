package game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurnManager {
    private final List<Combatant> combatants = new ArrayList<>();
    private final Map<String, Integer> nextTurnTicks = new HashMap<>();
    private int currentTick;

    public TurnManager(List<Combatant> combatants) {
        this.combatants.addAll(combatants);
        for (Combatant combatant : combatants) {
            nextTurnTicks.put(combatant.getId(), initialDelayFor(combatant));
        }
    }

    public Combatant getCurrentTurn() {
        return combatants.stream()
                .filter(Combatant::isAlive)
                .min(Comparator.comparingInt(c -> nextTurnTicks.getOrDefault(c.getId(), Integer.MAX_VALUE)))
                .orElse(null);
    }

    public void advanceAfterTurn(Combatant combatant) {
        advanceAfterTurn(combatant, 0);
    }

    public void advanceAfterTurn(Combatant combatant, int delayModifier) {
        if (combatant == null) {
            return;
        }
        currentTick = Math.max(currentTick, nextTurnTicks.getOrDefault(combatant.getId(), currentTick));
        nextTurnTicks.put(combatant.getId(), currentTick + delayFor(combatant, delayModifier));
    }

    public int getCurrentTick() {
        return currentTick;
    }

    private int initialDelayFor(Combatant combatant) {
        return Math.max(1, delayFor(combatant) / 2);
    }

    private int delayFor(Combatant combatant) {
        return delayFor(combatant, 0);
    }

    private int delayFor(Combatant combatant, int delayModifier) {
        return Math.max(8, 100 - combatant.getAgi() * 4 + delayModifier);
    }
}
