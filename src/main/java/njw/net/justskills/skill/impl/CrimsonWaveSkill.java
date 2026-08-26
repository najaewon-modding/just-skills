package njw.net.justskills.skill.impl;

import njw.net.justskills.skill.Skill;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.runtime.CrimsonWaveManager;

public final class CrimsonWaveSkill
        implements Skill {

    @Override
    public boolean activate(
            SkillContext context
    ) {

        CrimsonWaveManager.spawn(
                context.player()
        );

        return true;
    }
}