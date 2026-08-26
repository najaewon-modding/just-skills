package njw.net.justskills.unlock;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface SkillUnlockCondition {

    boolean isUnlocked(ServerPlayer player);
}