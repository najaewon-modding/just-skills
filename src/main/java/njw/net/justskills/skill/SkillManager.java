package njw.net.justskills.skill;

import net.minecraft.ChatFormatting;
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
    private SkillManager() {}

    public static final long SKILL_COOLDOWN_TICKS = 20L * 60L * 5L;

    public static void tryUseCurrentSkill(ServerPlayer player) {
        if (CastManager.isCasting(player)) return;

        PlayerSkillState state = getState(player);
        long now = player.level().getGameTime();

        if (state.currentSkill().isEmpty()) {
            if (state.isCoolingDown(now)) {
                showCooldownMessage(player, state.cooldownEndTick() - now);
                return;
            }

            assignRandomSkill(player);
            return;
        }

        Optional<SkillDefinition> optional = SkillRegistry.get(state.currentSkill().get());

        if (optional.isEmpty()) {
            setState(player, PlayerSkillState.empty());
            showNoAvailableSkillsMessage(player);
            return;
        }

        SkillDefinition definition = optional.get();

        if (!definition.unlockCondition().isUnlocked(player)) {
            setState(player, PlayerSkillState.empty());
            showNoAvailableSkillsMessage(player);
            return;
        }

        startSkill(player, definition);
    }

    private static void startSkill(ServerPlayer player, SkillDefinition definition) {
        if (definition.castSpec().isInstant()) {
            activateAndStartCooldown(player, definition);
            return;
        }

        CastManager.start(player, definition);
    }

    public static void completeCast(ServerPlayer player, SkillDefinition definition) {
        PlayerSkillState state = getState(player);

        if (state.currentSkill().isEmpty()) return;
        if (!state.currentSkill().get().equals(definition.id())) return;

        if (!definition.unlockCondition().isUnlocked(player)) {
            setState(player, PlayerSkillState.empty());
            showNoAvailableSkillsMessage(player);
            return;
        }

        activateAndStartCooldown(player, definition);
    }

    private static void activateAndStartCooldown(ServerPlayer player, SkillDefinition definition) {
        ServerLevel level = player.level();
        SkillContext context = new SkillContext(player, level);

        if (!definition.skill().activate(context)) return;

        long cooldownEndTick = level.getGameTime() + SKILL_COOLDOWN_TICKS;
        setState(player, getState(player).startCooldown(cooldownEndTick));
    }

    private static void assignRandomSkill(ServerPlayer player) {
        List<SkillDefinition> unlocked = new ArrayList<>();

        for (SkillDefinition definition : SkillRegistry.values()) {
            if (definition.unlockCondition().isUnlocked(player)) unlocked.add(definition);
        }

        if (unlocked.isEmpty()) {
            showNoAvailableSkillsMessage(player);
            return;
        }

        SkillDefinition selected = unlocked.get(player.level().getRandom().nextInt(unlocked.size()));
        setState(player, getState(player).withSkill(selected.id()));
        player.sendSystemMessage(Component.translatable(
                "message.njw_just_skills.new_skill",
                selected.displayName()
        ));
    }

    private static void showCooldownMessage(ServerPlayer player, long remainingTicks) {
        long remainingSeconds = Math.max(1L, (remainingTicks + 19L) / 20L);
        long minutes = remainingSeconds / 60L;
        long seconds = remainingSeconds % 60L;

        player.sendOverlayMessage(
                Component.translatable(
                        "message.njw_just_skills.cooldown_remaining",
                        minutes,
                        seconds
                ).withStyle(ChatFormatting.RED)
        );
    }

    private static void showNoAvailableSkillsMessage(ServerPlayer player) {
        player.sendOverlayMessage(
                Component.translatable("message.njw_just_skills.no_available_skills")
                        .withStyle(ChatFormatting.RED)
        );
    }

    public static PlayerSkillState getState(ServerPlayer player) {
        return player.getData(ModAttachments.PLAYER_SKILL_STATE.get());
    }

    private static void setState(ServerPlayer player, PlayerSkillState state) {
        player.setData(ModAttachments.PLAYER_SKILL_STATE.get(), state);
        player.syncData(ModAttachments.PLAYER_SKILL_STATE.get());
    }
}