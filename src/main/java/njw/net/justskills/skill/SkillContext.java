package njw.net.justskills.skill;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record SkillContext(
        ServerPlayer player,
        ServerLevel level
) {
}