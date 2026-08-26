package njw.net.justskills.skill.impl;

import njw.net.justskills.skill.Skill;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.runtime.AmberDiskManager;

public final class AmberDiskSkill
        implements Skill {

    @Override
    public boolean activate(
            SkillContext context
    ) {

        AmberDiskManager.spawn(
                context.player()
        );

        return true;
    }
}