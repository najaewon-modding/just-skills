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

    /*
     * ============================================================
     * Cooldown
     * ============================================================
     *
     * 현재 테스트용 10초.
     *
     * 나중에 5분:
     *
     * 20L * 60L * 5L
     */

    public static final long SKILL_COOLDOWN_TICKS =
            20L * 10L;

    /*
     * ============================================================
     * Use key
     * ============================================================
     */

    public static void tryUseCurrentSkill(
            ServerPlayer player
    ) {

        /*
         * 이미 시전 중이라면
         * 추가 입력 무시.
         */
        if (CastManager.isCasting(player)) {
            return;
        }

        PlayerSkillState state =
                getState(player);

        long now =
                player.level()
                        .getGameTime();

        /*
         * ========================================================
         * 현재 보유한 스킬이 없는 경우
         * ========================================================
         */

        if (state.currentSkill()
                .isEmpty()) {

            /*
             * 쿨다운 중이라면
             * 아무것도 하지 않는다.
             */
            if (state.isCoolingDown(now)) {
                return;
            }

            /*
             * 쿨다운이 끝난 상태에서 G를 눌렀다.
             *
             * 여기서는 랜덤 스킬을 배정하기만 한다.
             *
             * 중요:
             * 이번 G 입력으로는 시전하지 않는다.
             */
            assignRandomSkill(
                    player
            );

            return;
        }

        /*
         * ========================================================
         * 현재 스킬을 보유한 경우
         * ========================================================
         */

        Optional<SkillDefinition> optional =
                SkillRegistry.get(
                        state.currentSkill()
                                .get()
                );

        /*
         * 저장 데이터에는 있지만
         * 현재 registry에서 사라진 스킬인 경우.
         */
        if (optional.isEmpty()) {

            setState(
                    player,
                    PlayerSkillState.empty()
            );

            return;
        }

        SkillDefinition definition =
                optional.get();

        /*
         * 더 이상 해금 조건을 만족하지 않는 경우.
         */
        if (!definition.unlockCondition()
                .isUnlocked(player)) {

            return;
        }

        /*
         * 스킬을 이미 가지고 있으므로
         * 이번 G 입력에서는 실제 시전.
         */
        startSkill(
                player,
                definition
        );
    }

    /*
     * ============================================================
     * Start skill
     * ============================================================
     */

    private static void startSkill(
            ServerPlayer player,
            SkillDefinition definition
    ) {

        /*
         * Instant skill이면
         * 즉시 발동 후 cooldown.
         */
        if (definition.castSpec()
                .isInstant()) {

            activateAndStartCooldown(
                    player,
                    definition
            );

            return;
        }

        /*
         * 일반적인 cast skill.
         */
        CastManager.start(
                player,
                definition
        );
    }

    /*
     * ============================================================
     * Cast complete
     * ============================================================
     */

    public static void completeCast(
            ServerPlayer player,
            SkillDefinition definition
    ) {

        PlayerSkillState state =
                getState(player);

        /*
         * 시전이 끝났는데
         * 이미 현재 스킬이 사라진 경우.
         */
        if (state.currentSkill()
                .isEmpty()) {

            return;
        }

        /*
         * 시전을 시작했던 스킬과
         * 현재 보유 스킬이 다른 경우.
         */
        if (!state.currentSkill()
                .get()
                .equals(
                        definition.id()
                )) {

            return;
        }

        /*
         * 시전 완료 시점에도
         * 해금 여부를 다시 확인.
         */
        if (!definition.unlockCondition()
                .isUnlocked(player)) {

            return;
        }

        activateAndStartCooldown(
                player,
                definition
        );
    }

    /*
     * ============================================================
     * Activate
     * ============================================================
     */

    private static void activateAndStartCooldown(
            ServerPlayer player,
            SkillDefinition definition
    ) {

        ServerLevel level =
                player.level();

        SkillContext context =
                new SkillContext(
                        player,
                        level
                );

        boolean success =
                definition.skill()
                        .activate(
                                context
                        );

        /*
         * 실제 스킬 발동에 실패했다면:
         *
         * 스킬 유지
         * cooldown 없음
         */
        if (!success) {
            return;
        }

        /*
         * ========================================================
         * 성공
         * ========================================================
         *
         * 현재 스킬 제거.
         *
         * 여기서부터 10초 cooldown.
         */

        long cooldownEndTick =
                level.getGameTime()
                        + SKILL_COOLDOWN_TICKS;

        setState(
                player,
                getState(player)
                        .startCooldown(
                                cooldownEndTick
                        )
        );
    }

    /*
     * ============================================================
     * Random skill
     * ============================================================
     */

    private static void assignRandomSkill(
            ServerPlayer player
    ) {

        List<SkillDefinition> unlocked =
                new ArrayList<>();

        /*
         * 현재 해금된 모든 스킬을 수집.
         */
        for (SkillDefinition definition
                : SkillRegistry.values()) {

            if (definition.unlockCondition()
                    .isUnlocked(player)) {

                unlocked.add(
                        definition
                );
            }
        }

        /*
         * 해금된 스킬이 하나도 없다.
         */
        if (unlocked.isEmpty()) {
            return;
        }

        /*
         * 랜덤 선택.
         */
        SkillDefinition selected =
                unlocked.get(
                        player.level()
                                .getRandom()
                                .nextInt(
                                        unlocked.size()
                                )
                );

        /*
         * 실제 보유 스킬로 저장.
         *
         * cooldown은 이 시점에 종료 상태.
         */
        setState(
                player,
                getState(player)
                        .withSkill(
                                selected.id()
                        )
        );

        /*
         * 플레이어에게 새 스킬 알림.
         */
        player.sendSystemMessage(
                Component.translatable(
                        "message.jwn_just_skills.new_skill",
                        selected.displayName()
                )
        );
    }

    /*
     * ============================================================
     * Attachment
     * ============================================================
     */

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

        /*
         * HUD 즉시 갱신.
         */
        player.syncData(
                ModAttachments
                        .PLAYER_SKILL_STATE
                        .get()
        );
    }
}