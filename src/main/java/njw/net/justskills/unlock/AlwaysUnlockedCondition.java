package njw.net.justskills.unlock;

import net.minecraft.server.level.ServerPlayer;

public enum AlwaysUnlockedCondition
        implements SkillUnlockCondition {

    INSTANCE;

    @Override
    public boolean isUnlocked(ServerPlayer player) {
        return true;
    }
}