package njw.net.justskills.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import njw.net.justskills.data.ModAttachments;
import njw.net.justskills.data.PlayerSkillState;
import njw.net.justskills.skill.cast.CastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SkillManager {

    private SkillManager() {
    }

    public static final long SKILL_CYCLE_TICKS =
            20L * 60L * 5L;

    public static void tickPlayer(
            ServerPlayer player
    ) {

        PlayerSkillState state =
                getState(player);

        long now = player.level().getGameTime();

        if (state.nextRollTick() <= 0L) {
            rollNewSkill(player, now);
            return;
        }

        if (now >= state.nextRollTick()) {
            rollNewSkill(player, now);
            return;
        }

        if (state.currentSkill().isPresent()) {

            Optional<SkillDefinition> definition =
                    SkillRegistry.get(
                            state.currentSkill().get()
                    );

            // 이전 버전에서 존재했던 스킬이
            // 삭제된 경우도 안전하게 처리.
            if (definition.isEmpty()) {
                rollNewSkill(player, now);
            }
        }
    }

    public static void tryUseCurrentSkill(
            ServerPlayer player
    ) {

        if (CastManager.isCasting(player)) {
            return;
        }

        PlayerSkillState state =
                getState(player);

        if (state.used()
        ) {

            player.sendSystemMessage(
                    Component.translatable(
                            "message.jwn_just_skills.already_used"
                    )
            );

            return;
        }

        if (state.currentSkill().isEmpty()) {
            return;
        }

        Optional<SkillDefinition> optional =
                SkillRegistry.get(
                        state.currentSkill().get()
                );

        if (optional.isEmpty()) {
            return;
        }

        SkillDefinition definition =
                optional.get();

        if (!definition.unlockCondition()
                .isUnlocked(player)) {

            return;
        }

        if (definition.castSpec().isInstant()) {

            activateAndConsume(
                    player,
                    definition
            );

            return;
        }

        CastManager.start(
                player,
                definition
        );
    }

    public static void completeCast(
            ServerPlayer player,
            SkillDefinition definition
    ) {

        PlayerSkillState state =
                getState(player);

        // 시전 중 5분 사이클이 바뀌었다든지 하는
        // 예외 상황에 대한 안전장치.
        if (state.currentSkill().isEmpty()
                || !state.currentSkill()
                .get()
                .equals(definition.id())) {

            return;
        }

        if (state.used()) {
            return;
        }

        if (!definition.unlockCondition()
                .isUnlocked(player)) {

            return;
        }

        activateAndConsume(
                player,
                definition
        );
    }

    private static void activateAndConsume(
            ServerPlayer player,
            SkillDefinition definition
    ) {
        ServerLevel level = player.level();

        SkillContext context =
                new SkillContext(
                        player,
                        level
                );

        boolean success =
                definition.skill()
                        .activate(context);

        if (!success) {
            return;
        }

        PlayerSkillState state =
                getState(player);

        setState(
                player,
                state.markUsed()
        );
    }

    private static void rollNewSkill(
            ServerPlayer player,
            long now
    ) {

        CastManager.cancel(player);

        List<SkillDefinition> unlocked =
                new ArrayList<>();

        for (SkillDefinition definition
                : SkillRegistry.values()) {

            if (definition.unlockCondition()
                    .isUnlocked(player)) {

                unlocked.add(definition);
            }
        }

        long nextRoll =
                now + SKILL_CYCLE_TICKS;

        if (unlocked.isEmpty()) {

            setState(
                    player,
                    getState(player)
                            .withoutSkill(nextRoll)
            );

            return;
        }

        SkillDefinition selected =
                unlocked.get(
                        player.level()
                                .getRandom()
                                .nextInt(
                                        unlocked.size()
                                )
                );

        setState(
                player,
                getState(player)
                        .withSkill(
                                selected.id(),
                                nextRoll
                        )
        );

        player.sendSystemMessage(
                Component.translatable(
                        "message.jwn_just_skills.new_skill",
                        selected.displayName()
                )
        );
    }

    public static PlayerSkillState getState(
            ServerPlayer player
    ) {
        return player.getData(
                ModAttachments
                        .PLAYER_SKILL_STATE
                        .get()
        );
    }

    private static void setState(
            ServerPlayer player,
            PlayerSkillState state
    ) {
        player.setData(
                ModAttachments
                        .PLAYER_SKILL_STATE
                        .get(),
                state
        );
    }
}