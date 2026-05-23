package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;

class SkillTree {
    private final Map<String, Skill> skills = new LinkedHashMap<>();
    private final Set<String> unlocked = new HashSet<>();

    public SkillTree() {
        seedSkills();
    }

    public List<Skill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }

    public Skill getSkill(String id) {
        return skills.get(id);
    }

    public List<Skill> getSkillsByCategory(SkillCategory category) {
        List<Skill> result = new ArrayList<>();
        for (Skill skill : skills.values()) {
            if (skill.getCategory() == category) {
                result.add(skill);
            }
        }
        return result;
    }

    public List<Skill> getUnlockedSkillsByCategory(SkillCategory category) {
        List<Skill> result = new ArrayList<>();
        for (Skill skill : skills.values()) {
            if (skill.getCategory() == category && unlocked.contains(skill.getId())) {
                result.add(skill);
            }
        }
        return result;
    }

    public boolean isUnlocked(String id) {
        return unlocked.contains(id);
    }

    public boolean canUnlock(String id) {
        Skill skill = skills.get(id);
        if (skill == null || unlocked.contains(id)) {
            return false;
        }
        for (String prereq : skill.getPrerequisites()) {
            if (!unlocked.contains(prereq)) {
                return false;
            }
        }
        return true;
    }

    public boolean unlock(String id) {
        if (!canUnlock(id)) {
            return false;
        }
        unlocked.add(id);
        return true;
    }

    public void save(Properties properties) {
        StringJoiner joiner = new StringJoiner(",");
        for (String id : unlocked) {
            joiner.add(id);
        }
        properties.setProperty("player.skills.unlocked", joiner.toString());
    }

    public void load(Properties properties) {
        unlocked.clear();
        String raw = properties.getProperty("player.skills.unlocked", "");
        if (raw.trim().isEmpty()) {
            return;
        }
        for (String id : raw.split(",")) {
            String clean = id.trim();
            if (skills.containsKey(clean)) {
                unlocked.add(clean);
            }
        }
    }

    public int getPassiveBonusStr() {
        int total = 0;
        for (Skill skill : skills.values()) {
            if (skill.getType() == SkillType.PASSIVE && unlocked.contains(skill.getId())) {
                total += skill.getBonusStr();
            }
        }
        return total;
    }

    public int getPassiveBonusDef() {
        int total = 0;
        for (Skill skill : skills.values()) {
            if (skill.getType() == SkillType.PASSIVE && unlocked.contains(skill.getId())) {
                total += skill.getBonusDef();
            }
        }
        return total;
    }

    public int getPassiveBonusAgi() {
        int total = 0;
        for (Skill skill : skills.values()) {
            if (skill.getType() == SkillType.PASSIVE && unlocked.contains(skill.getId())) {
                total += skill.getBonusAgi();
            }
        }
        return total;
    }

    public int getPassiveBonusLuck() {
        int total = 0;
        for (Skill skill : skills.values()) {
            if (skill.getType() == SkillType.PASSIVE && unlocked.contains(skill.getId())) {
                total += skill.getBonusLuck();
            }
        }
        return total;
    }

    public int getPassiveBonusHp() {
        int total = 0;
        for (Skill skill : skills.values()) {
            if (skill.getType() == SkillType.PASSIVE && unlocked.contains(skill.getId())) {
                total += skill.getBonusHp();
            }
        }
        return total;
    }

    public int getPassiveBonusMp() {
        int total = 0;
        for (Skill skill : skills.values()) {
            if (skill.getType() == SkillType.PASSIVE && unlocked.contains(skill.getId())) {
                total += skill.getBonusMp();
            }
        }
        return total;
    }

    private void seedSkills() {
        add(new Skill("atk1", "Blade Basics", SkillCategory.ATTACK, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList(), 1, 0, 0, 0, 0, 0));
        add(new Skill("atk2", "Power Slash", SkillCategory.ATTACK, SkillType.ACTIVE, SkillAction.PHYSICAL, 4, 8, 5, Arrays.asList("atk1"), 0, 0, 0, 0, 0, 0));
        add(new Skill("atk3", "Double Strike", SkillCategory.ATTACK, SkillType.ACTIVE, SkillAction.PHYSICAL, 6, 10, 8, Arrays.asList("atk2"), 0, 0, 0, 0, 0, 0));
        add(new Skill("atk4", "Keen Edge", SkillCategory.ATTACK, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("atk2"), 0, 0, 0, 2, 0, 0));
        add(new Skill("atk5", "Whirlwind", SkillCategory.ATTACK, SkillType.ACTIVE, SkillAction.PHYSICAL, 8, 14, 5, Arrays.asList("atk3"), 0, 0, 0, 0, 0, 0));
        add(new Skill("atk6", "Crushing Blow", SkillCategory.ATTACK, SkillType.ACTIVE, SkillAction.PHYSICAL, 10, 18, -5, Arrays.asList("atk3"), 0, 0, 0, 0, 0, 0));
        add(new Skill("atk7", "Battle Instinct", SkillCategory.ATTACK, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("atk4"), 2, 0, 1, 0, 0, 0));
        add(new Skill("atk8", "Executioner", SkillCategory.ATTACK, SkillType.ACTIVE, SkillAction.PHYSICAL, 12, 22, 0, Arrays.asList("atk6"), 0, 0, 0, 0, 0, 0));

        add(new Skill("def1", "Guard Stance", SkillCategory.DEFENSE, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList(), 0, 2, 0, 0, 0, 0));
        add(new Skill("def2", "Fortify", SkillCategory.DEFENSE, SkillType.ACTIVE, SkillAction.GUARD, 4, 0, 0, Arrays.asList("def1"), 0, 0, 0, 0, 0, 0));
        add(new Skill("def3", "Iron Skin", SkillCategory.DEFENSE, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("def1"), 0, 2, 0, 0, 10, 0));
        add(new Skill("def4", "Endure", SkillCategory.DEFENSE, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("def3"), 0, 1, 0, 0, 15, 0));
        add(new Skill("def5", "Counter Guard", SkillCategory.DEFENSE, SkillType.ACTIVE, SkillAction.PHYSICAL, 6, 10, 10, Arrays.asList("def2"), 0, 0, 0, 0, 0, 0));
        add(new Skill("def6", "Bulwark", SkillCategory.DEFENSE, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("def4"), 0, 3, -1, 0, 0, 0));

        add(new Skill("off1", "Spark", SkillCategory.OFFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.MAGIC, 6, 10, 5, Arrays.asList(), 0, 0, 0, 0, 0, 0));
        add(new Skill("off2", "Flame", SkillCategory.OFFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.MAGIC, 8, 14, 5, Arrays.asList("off1"), 0, 0, 0, 0, 0, 0));
        add(new Skill("off3", "Frost", SkillCategory.OFFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.MAGIC, 8, 14, 5, Arrays.asList("off1"), 0, 0, 0, 0, 0, 0));
        add(new Skill("off4", "Storm", SkillCategory.OFFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.MAGIC, 10, 18, 0, Arrays.asList("off2"), 0, 0, 0, 0, 0, 0));
        add(new Skill("off5", "Arcane Focus", SkillCategory.OFFENSIVE_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("off1"), 0, 0, 0, 2, 0, 5));
        add(new Skill("off6", "Meteor", SkillCategory.OFFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.MAGIC, 14, 24, -5, Arrays.asList("off4"), 0, 0, 0, 0, 0, 0));
        add(new Skill("off7", "Flare", SkillCategory.OFFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.MAGIC, 16, 28, -5, Arrays.asList("off6"), 0, 0, 0, 0, 0, 0));
        add(new Skill("off8", "Magic Reserves", SkillCategory.OFFENSIVE_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("off5"), 0, 0, 0, 0, 0, 10));
        add(new Skill("off9", "Thunderlash", SkillCategory.OFFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.MAGIC, 12, 20, 5, Arrays.asList("off4"), 0, 0, 0, 0, 0, 0));
        add(new Skill("off10", "Elemental Mastery", SkillCategory.OFFENSIVE_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("off8"), 0, 0, 1, 2, 0, 0));

        add(new Skill("defm1", "Ward", SkillCategory.DEFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.GUARD, 6, 0, 0, Arrays.asList(), 0, 0, 0, 0, 0, 0));
        add(new Skill("defm2", "Barrier", SkillCategory.DEFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.GUARD, 8, 0, 0, Arrays.asList("defm1"), 0, 0, 0, 0, 0, 0));
        add(new Skill("defm3", "Mana Veil", SkillCategory.DEFENSIVE_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("defm1"), 0, 1, 0, 1, 0, 5));
        add(new Skill("defm4", "Mirror Guard", SkillCategory.DEFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.GUARD, 10, 0, 0, Arrays.asList("defm2"), 0, 0, 0, 0, 0, 0));
        add(new Skill("defm5", "Aegis", SkillCategory.DEFENSIVE_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("defm3"), 0, 2, 0, 0, 5, 0));
        add(new Skill("defm6", "Mind Shield", SkillCategory.DEFENSIVE_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("defm3"), 0, 0, 0, 2, 0, 5));
        add(new Skill("defm7", "Spell Guard", SkillCategory.DEFENSIVE_MAGIC, SkillType.ACTIVE, SkillAction.GUARD, 12, 0, 0, Arrays.asList("defm4"), 0, 0, 0, 0, 0, 0));
        add(new Skill("defm8", "Mystic Armor", SkillCategory.DEFENSIVE_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("defm5"), 0, 2, 0, 0, 10, 0));

        add(new Skill("sup1", "First Aid", SkillCategory.SUPPORT_MAGIC, SkillType.ACTIVE, SkillAction.HEAL, 6, 18, 0, Arrays.asList(), 0, 0, 0, 0, 0, 0));
        add(new Skill("sup2", "Heal", SkillCategory.SUPPORT_MAGIC, SkillType.ACTIVE, SkillAction.HEAL, 10, 26, 0, Arrays.asList("sup1"), 0, 0, 0, 0, 0, 0));
        add(new Skill("sup3", "Focus", SkillCategory.SUPPORT_MAGIC, SkillType.ACTIVE, SkillAction.RESTORE_MP, 0, 12, 0, Arrays.asList("sup1"), 0, 0, 0, 0, 0, 0));
        add(new Skill("sup4", "Inspiration", SkillCategory.SUPPORT_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("sup1"), 0, 0, 1, 1, 0, 0));
        add(new Skill("sup5", "Greater Heal", SkillCategory.SUPPORT_MAGIC, SkillType.ACTIVE, SkillAction.HEAL, 14, 34, 0, Arrays.asList("sup2"), 0, 0, 0, 0, 0, 0));
        add(new Skill("sup6", "Meditation", SkillCategory.SUPPORT_MAGIC, SkillType.ACTIVE, SkillAction.RESTORE_MP, 0, 20, 0, Arrays.asList("sup3"), 0, 0, 0, 0, 0, 0));
        add(new Skill("sup7", "Spirit Bond", SkillCategory.SUPPORT_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("sup4"), 0, 0, 0, 2, 5, 5));
        add(new Skill("sup8", "Revitalize", SkillCategory.SUPPORT_MAGIC, SkillType.PASSIVE, SkillAction.NONE, 0, 0, 0, Arrays.asList("sup5"), 0, 0, 0, 1, 15, 10));
    }

    private void add(Skill skill) {
        skills.put(skill.getId(), skill);
    }
}

