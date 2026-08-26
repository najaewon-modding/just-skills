package njw.net.justskills.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import njw.net.justskills.skill.cast.CastSpec;
import njw.net.justskills.unlock.SkillUnlockCondition;

public record SkillDefinition(
        Identifier id,
        String translationKey,
        Skill skill,
        CastSpec castSpec,
        SkillUnlockCondition unlockCondition
) {

    public Component displayName() {
        return Component.translatable(translationKey);
    }
}