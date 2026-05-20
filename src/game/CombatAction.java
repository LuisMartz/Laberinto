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

    private CombatAction(Type type, Skill skill) {
        this.type = type;
        this.skill = skill;
    }

    public static CombatAction attack() {
        return new CombatAction(Type.ATTACK, null);
    }

    public static CombatAction defend() {
        return new CombatAction(Type.DEFEND, null);
    }

    public static CombatAction skill(Skill skill) {
        return new CombatAction(Type.SKILL, skill);
    }

    public static CombatAction item() {
        return new CombatAction(Type.ITEM, null);
    }

    public Type getType() {
        return type;
    }

    public Skill getSkill() {
        return skill;
    }
}
