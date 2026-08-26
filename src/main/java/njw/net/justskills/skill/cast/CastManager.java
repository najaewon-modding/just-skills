package njw.net.justskills.skill.cast;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import njw.net.justskills.JustSkills;
import njw.net.justskills.network.CastStatePayload;
import njw.net.justskills.skill.SkillDefinition;
import njw.net.justskills.skill.SkillManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CastManager {

    private CastManager() {
    }

    private static final Map<UUID, CastSession>
            ACTIVE_CASTS =
            new HashMap<>();

    /*
     * ============================================================
     * Movement lock
     * ============================================================
     */

    private static final Identifier MOVEMENT_LOCK_ID =
            Identifier.fromNamespaceAndPath(
                    JustSkills.MODID,
                    "casting_movement_lock"
            );

    private static final AttributeModifier MOVEMENT_LOCK =
            new AttributeModifier(
                    MOVEMENT_LOCK_ID,
                    -1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

    /*
     * ============================================================
     * Public
     * ============================================================
     */

    public static boolean isCasting(
            ServerPlayer player
    ) {

        return ACTIVE_CASTS.containsKey(
                player.getUUID()
        );
    }

    public static boolean isMovementLocked(
            ServerPlayer player
    ) {

        CastSession session =
                ACTIVE_CASTS.get(
                        player.getUUID()
                );

        return session != null
                && session.skill()
                .castSpec()
                .lockMovement();
    }

    /*
     * ============================================================
     * Start
     * ============================================================
     */

    public static boolean start(
            ServerPlayer player,
            SkillDefinition skill
    ) {

        if (isCasting(player)) {
            return false;
        }

        CastSpec spec =
                skill.castSpec();

        if (spec.isInstant()) {
            return false;
        }

        /*
         * --------------------------------------------------------
         * Boss bar
         * --------------------------------------------------------
         */

        ServerBossEvent bossBar = null;

        if (spec.showBossBar()) {

            bossBar =
                    new ServerBossEvent(
                            UUID.randomUUID(),

                            Component.translatable(
                                    "message.njw_just_skills.casting",
                                    skill.displayName()
                            ),

                            BossEvent.BossBarColor.RED,
                            BossEvent.BossBarOverlay.PROGRESS
                    );

            bossBar.setProgress(0.0F);

            bossBar.addPlayer(player);
        }

        /*
         * --------------------------------------------------------
         * Session
         * --------------------------------------------------------
         */

        CastSession session =
                new CastSession(
                        skill,
                        player.position(),
                        player.level().dimension(),
                        bossBar
                );

        ACTIVE_CASTS.put(
                player.getUUID(),
                session
        );

        /*
         * --------------------------------------------------------
         * Movement lock
         * --------------------------------------------------------
         */

        if (spec.lockMovement()) {

            applyMovementLock(player);

            /*
             * 클라이언트에게:
             *
             * 자발적 movement input을 막으라고 알린다.
             */
            syncMovementLock(
                    player,
                    true
            );
        }

        player.setSprinting(false);

        return true;
    }

    /*
     * ============================================================
     * Tick
     * ============================================================
     */

    public static void tick(
            ServerPlayer player
    ) {

        CastSession session =
                ACTIVE_CASTS.get(
                        player.getUUID()
                );

        if (session == null) {
            return;
        }

        CastSpec spec =
                session.skill()
                        .castSpec();

        /*
         * 죽으면 취소.
         */
        if (!player.isAlive()) {
            cancel(player);
            return;
        }

        /*
         * dimension이 바뀌면 취소.
         */
        if (!player.level()
                .dimension()
                .equals(
                        session.dimension()
                )) {

            cancel(player);
            return;
        }

        /*
         * sprint 강제 해제.
         */
        if (spec.lockMovement()) {
            player.setSprinting(false);
        }

        /*
         * 원래 위치에서 너무 멀어졌는지 검사.
         *
         * X/Z만 본다.
         *
         * 점프는 client input에서 차단되지만,
         * 넉백 등 외부 힘은 그대로 허용한다.
         */
        if (hasMovedTooFar(
                player,
                session.origin(),
                spec.maxDisplacement()
        )) {

            cancel(player);
            return;
        }

        /*
         * 진행.
         */
        session.tick();

        /*
         * Boss bar.
         */
        if (session.bossBar() != null) {

            float progress =
                    Math.min(
                            1.0F,
                            session.elapsedTicks()
                                    / (float)
                                    spec.durationTicks()
                    );

            session.bossBar()
                    .setProgress(progress);
        }

        /*
         * 완료.
         */
        if (session.elapsedTicks()
                >= spec.durationTicks()) {

            finish(player);
        }
    }

    /*
     * ============================================================
     * Distance
     * ============================================================
     */

    private static boolean hasMovedTooFar(
            ServerPlayer player,
            Vec3 origin,
            double maxDistance
    ) {

        double dx =
                player.getX()
                        - origin.x;

        double dz =
                player.getZ()
                        - origin.z;

        return dx * dx + dz * dz
                > maxDistance * maxDistance;
    }

    /*
     * ============================================================
     * Finish
     * ============================================================
     */

    private static void finish(
            ServerPlayer player
    ) {

        CastSession session =
                ACTIVE_CASTS.remove(
                        player.getUUID()
                );

        if (session == null) {
            return;
        }

        cleanup(
                player,
                session
        );

        SkillManager.completeCast(
                player,
                session.skill()
        );
    }

    /*
     * ============================================================
     * Cancel
     * ============================================================
     */

    public static void cancel(
            ServerPlayer player
    ) {

        CastSession session =
                ACTIVE_CASTS.remove(
                        player.getUUID()
                );

        if (session == null) {
            return;
        }

        cleanup(
                player,
                session
        );
    }

    /*
     * ============================================================
     * Cleanup
     * ============================================================
     */

    private static void cleanup(
            ServerPlayer player,
            CastSession session
    ) {

        if (session.skill()
                .castSpec()
                .lockMovement()) {

            removeMovementLock(player);

            /*
             * 클라이언트 movement input 복구.
             */
            syncMovementLock(
                    player,
                    false
            );
        }

        if (session.bossBar() != null) {

            session.bossBar()
                    .removePlayer(player);
        }
    }

    /*
     * ============================================================
     * Server-side movement modifier
     * ============================================================
     */

    private static void applyMovementLock(
            ServerPlayer player
    ) {

        AttributeInstance movement =
                player.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movement == null) {
            return;
        }

        movement.removeModifier(
                MOVEMENT_LOCK_ID
        );

        movement.addOrUpdateTransientModifier(
                MOVEMENT_LOCK
        );
    }

    private static void removeMovementLock(
            ServerPlayer player
    ) {

        AttributeInstance movement =
                player.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movement == null) {
            return;
        }

        movement.removeModifier(
                MOVEMENT_LOCK_ID
        );
    }

    /*
     * ============================================================
     * Client sync
     * ============================================================
     */

    private static void syncMovementLock(
            ServerPlayer player,
            boolean locked
    ) {

        PacketDistributor.sendToPlayer(
                player,
                new CastStatePayload(
                        locked
                )
        );
    }
}