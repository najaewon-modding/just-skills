package njw.net.justskills.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import njw.net.justskills.skill.cast.CastSpec;
import njw.net.justskills.unlock.SkillUnlockCondition;

public record SkillDefinition(
        Identifier id,
        String translationKey,
        Item iconItem,
        Skill skill,
        CastSpec castSpec,
        SkillUnlockCondition unlockCondition
) {

    public Component displayName() {

        return Component.translatable(
                translationKey
        );
    }

    public ItemStack iconStack() {

        return new ItemStack(
                iconItem
        );
    }
}