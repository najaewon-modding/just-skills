package njw.net.justskills.unlock;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class AdvancementUnlockCondition
        implements SkillUnlockCondition {

    private final Identifier advancementId;

    public AdvancementUnlockCondition(
            Identifier advancementId
    ) {
        this.advancementId = advancementId;
    }

    @Override
    public boolean isUnlocked(ServerPlayer player) {
        AdvancementHolder advancement =
                player.level()
                        .getServer()
                        .getAdvancements()
                        .get(advancementId);

        if (advancement == null) {
            return false;
        }

        return player.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone();
    }

    public Identifier advancementId() {
        return advancementId;
    }
}