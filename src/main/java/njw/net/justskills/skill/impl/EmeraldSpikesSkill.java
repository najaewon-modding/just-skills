package njw.net.justskills.skill.impl;

import njw.net.justskills.skill.Skill;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.runtime.EmeraldSpikesManager;

public final class EmeraldSpikesSkill
        implements Skill {

    @Override
    public boolean activate(
            SkillContext context
    ) {

        EmeraldSpikesManager.spawn(
                context.player()
        );

        return true;
    }
}