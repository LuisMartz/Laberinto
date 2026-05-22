package game;

import java.util.Random;

public class CombatResolver {
    private final Random random;

    public CombatResolver() {
        this(new Random());
    }

    public CombatResolver(Random random) {
        this.random = random;
    }

    public CombatResult resolvePhysicalAttack(Combatant attacker, Combatant defender, int accuracyBonus, int powerBonus) {
        boolean hit = rollHit(attacker.getAgi(), defender.getAgi(), attacker.getLuck(), accuracyBonus);
        if (!hit) {
            return new CombatResult(false, false, 0, attacker.getName() + " missed.");
        }
        boolean critical = rollCrit(attacker.getLuck());
        int damage = calculateDamage(attacker.getStr(), defender.getDef(), defender.isGuarding(), critical) + powerBonus;
        defender.applyDamage(damage);
        defender.setGuarding(false);
        return new CombatResult(true, critical, damage, attacker.getName() + (critical ? " crits" : " hits") + " for " + damage + ".");
    }

    public CombatResult resolveMagicAttack(Combatant attacker, Combatant defender, Skill skill) {
        boolean critical = rollCrit(attacker.getLuck());
        int damage = Math.max(1, skill.getPower() + attacker.getInt() * 2 + attacker.getLuck() / 2
                - defender.getInt() - defender.getDef() / 3);
        if (critical) {
            damage = (int) Math.round(damage * 1.5);
        }
        if (defender.isGuarding()) {
            damage = Math.max(1, damage / 2);
        }
        defender.applyDamage(damage);
        defender.setGuarding(false);
        return new CombatResult(true, critical, damage, skill.getName() + (critical ? " crits" : " hits") + " for " + damage + ".");
    }

    public int calculateDamage(int attackerStr, int defenderDef, boolean defenderGuarding, boolean critical) {
        int base = 5 + attackerStr * 2;
        int damage = Math.max(1, base - defenderDef);
        if (critical) {
            damage = (int) Math.round(damage * 1.5);
        }
        if (defenderGuarding) {
            damage = Math.max(1, damage / 2);
        }
        return damage;
    }

    public boolean rollHit(int attackerAgi, int defenderAgi, int attackerLuck, int accuracyBonus) {
        double chance = 0.75 + (attackerAgi - defenderAgi) * 0.03 + attackerLuck * 0.01 + accuracyBonus * 0.01;
        chance = Math.max(0.1, Math.min(0.95, chance));
        return random.nextDouble() < chance;
    }

    public boolean rollCrit(int attackerLuck) {
        double chance = 0.05 + attackerLuck * 0.01;
        chance = Math.max(0.05, Math.min(0.25, chance));
        return random.nextDouble() < chance;
    }
}
