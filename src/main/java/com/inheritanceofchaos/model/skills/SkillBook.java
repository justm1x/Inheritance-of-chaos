package com.inheritanceofchaos.model.skills;

import java.util.ArrayList;
import java.util.List;

// List of all the hero's skills, in the order they unlock.
public class SkillBook {

    private static List<Skill> allSkills = new ArrayList<>();

    static {
        allSkills.add(new PowerStrike());   // level 1
        allSkills.add(new SecondWind());    // level 3
        allSkills.add(new ShatterGuard());  // level 5
        allSkills.add(new ChaosBolt());     // level 7
        allSkills.add(new Bloodletting());  // level 9
    }

    public static List<Skill> all() {
        return allSkills;
    }

    // skills the hero can use at this level
    public static List<Skill> unlockedAt(int level) {
        List<Skill> result = new ArrayList<>();
        for (Skill skill : allSkills) {
            if (skill.getRequiredLevel() <= level) {
                result.add(skill);
            }
        }
        return result;
    }
}
