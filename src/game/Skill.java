package game;

import java.util.List;

class Skill {
    private final String id;
    private final String name;
    private final SkillCategory category;
    private final SkillType type;
    private final SkillAction action;
    private final int mpCost;
    private final int power;
    private final int accuracyBonus;
    private final List<String> prerequisites;
    private final int bonusStr;
    private final int bonusDef;
    private final int bonusAgi;
    private final int bonusLuck;
    private final int bonusHp;
    private final int bonusMp;

    public Skill(String id, String name, SkillCategory category, SkillType type, SkillAction action, int mpCost, int power, int accuracyBonus, List<String> prerequisites,
                 int bonusStr, int bonusDef, int bonusAgi, int bonusLuck, int bonusHp, int bonusMp) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.type = type;
        this.action = action;
        this.mpCost = mpCost;
        this.power = power;
        this.accuracyBonus = accuracyBonus;
        this.prerequisites = prerequisites;
        this.bonusStr = bonusStr;
        this.bonusDef = bonusDef;
        this.bonusAgi = bonusAgi;
        this.bonusLuck = bonusLuck;
        this.bonusHp = bonusHp;
        this.bonusMp = bonusMp;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public SkillType getType() {
        return type;
    }

    public SkillAction getAction() {
        return action;
    }

    public int getMpCost() {
        return mpCost;
    }

    public int getPower() {
        return power;
    }

    public int getAccuracyBonus() {
        return accuracyBonus;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public int getBonusStr() {
        return bonusStr;
    }

    public int getBonusDef() {
        return bonusDef;
    }

    public int getBonusAgi() {
        return bonusAgi;
    }

    public int getBonusLuck() {
        return bonusLuck;
    }

    public int getBonusHp() {
        return bonusHp;
    }

    public int getBonusMp() {
        return bonusMp;
    }

    public String getShortDescription() {
        if (type == SkillType.PASSIVE) {
            StringBuilder sb = new StringBuilder();
            appendStat(sb, "STR", bonusStr);
            appendStat(sb, "DEF", bonusDef);
            appendStat(sb, "AGI", bonusAgi);
            appendStat(sb, "LCK", bonusLuck);
            appendStat(sb, "HP", bonusHp);
            appendStat(sb, "MP", bonusMp);
            if (sb.length() == 0) {
                return "Passive";
            }
            return sb.toString();
        }
        return "MP " + mpCost + " POW " + power;
    }

    private void appendStat(StringBuilder sb, String label, int value) {
        if (value == 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append(label).append("+").append(value);
    }
}

