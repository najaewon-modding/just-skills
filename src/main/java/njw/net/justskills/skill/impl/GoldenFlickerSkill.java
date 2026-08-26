package njw.net.justskills.skill.impl;

import njw.net.justskills.skill.Skill;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.runtime.GoldenFlickerManager;

public final class GoldenFlickerSkill
        implements Skill {

    @Override
    public boolean activate(
            SkillContext context
    ) {

        GoldenFlickerManager.spawn(
                context.player()
        );

        return true;
    }
}