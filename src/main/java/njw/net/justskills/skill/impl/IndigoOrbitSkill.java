package njw.net.justskills.skill.impl;

import njw.net.justskills.skill.Skill;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.runtime.IndigoOrbitManager;

public final class IndigoOrbitSkill
        implements Skill {

    @Override
    public boolean activate(
            SkillContext context
    ) {

        IndigoOrbitManager.spawn(
                context.player()
        );

        return true;
    }
}