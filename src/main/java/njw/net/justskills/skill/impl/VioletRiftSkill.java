package njw.net.justskills.skill.impl;

import njw.net.justskills.skill.Skill;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.runtime.VioletRiftManager;

public final class VioletRiftSkill implements Skill {
    @Override
    public boolean activate(SkillContext context) {
        VioletRiftManager.spawn(context.player());
        return true;
    }
}