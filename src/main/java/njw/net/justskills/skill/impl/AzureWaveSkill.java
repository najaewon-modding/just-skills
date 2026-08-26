package njw.net.justskills.skill.impl;

import njw.net.justskills.skill.Skill;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.runtime.AzureWaveManager;

public final class AzureWaveSkill
        implements Skill {

    @Override
    public boolean activate(
            SkillContext context
    ) {

        AzureWaveManager.spawn(
                context.player()
        );

        return true;
    }
}