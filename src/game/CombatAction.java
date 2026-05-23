package game;

public class CombatAction {
    public enum Type {
        ATTACK,
        DEFEND,
        SKILL,
        ITEM
    }

    private final Type type;
    private final Skill skill;
    private final int delayModifier;

    private CombatAction(Type type, Skill skill, int delayModifier) {
        this.type = type;
        this.skill = skill;
        this.delayModifier = delayModifier;
    }

    public static CombatAction attack() {
        return new CombatAction(Type.ATTACK, null, 0);
    }

    public static CombatAction defend() {
        return new CombatAction(Type.DEFEND, null, -18);
    }

    public static CombatAction skill(Skill skill) {
        int delayModifier = skill == null ? 0 : Math.max(0, skill.getPower() / 2 + skill.getMpCost() / 2);
        return new CombatAction(Type.SKILL, skill, delayModifier);
    }

    public static CombatAction item() {
        return new CombatAction(Type.ITEM, null, -8);
    }

    public Type getType() {
        return type;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getDelayModifier() {
        return delayModifier;
    }
}
