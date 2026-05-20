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
        if (combatant == null) {
            return;
        }
        currentTick = Math.max(currentTick, nextTurnTicks.getOrDefault(combatant.getId(), currentTick));
        nextTurnTicks.put(combatant.getId(), currentTick + delayFor(combatant));
    }

    public int getCurrentTick() {
        return currentTick;
    }

    private int initialDelayFor(Combatant combatant) {
        return Math.max(1, delayFor(combatant) / 2);
    }

    private int delayFor(Combatant combatant) {
        return Math.max(12, 100 - combatant.getAgi() * 4);
    }
}
