package njw.net.justskills.skill.runtime.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class RadialDamage {

    private RadialDamage() {
    }

    /**
     * 중심에서 radius까지 도달한 적대적 몹에게
     * 스킬 1회 발동당 최대 한 번 피해를 준다.
     *
     * Enemy를 구현한 엔티티만 대상이 된다.
     *
     * 따라서:
     *
     * Zombie       O
     * Skeleton     O
     * Creeper      O
     * Spider       O
     * Slime        O
     * Ghast        O
     * Hoglin       O
     * Warden       O
     *
     * Cow          X
     * Pig          X
     * Villager     X
     * Iron Golem   X
     * Player       X
     */
    public static void damageHostilesOnce(
            ServerLevel level,
            ServerPlayer ownerPlayer,
            Vec3 center,
            double radius,
            double verticalRange,
            double hitPadding,
            float damage,
            Set<UUID> hitEntities
    ) {

        if (radius <= 0.0) {
            return;
        }

        /*
         * ========================================================
         * Search area
         * ========================================================
         */

        AABB searchArea =
                new AABB(
                        center.x
                                - radius
                                - hitPadding,

                        center.y
                                - verticalRange,

                        center.z
                                - radius
                                - hitPadding,

                        center.x
                                + radius
                                + hitPadding,

                        center.y
                                + verticalRange,

                        center.z
                                + radius
                                + hitPadding
                );

        /*
         * Enemy를 구현한 살아있는 엔티티만 가져온다.
         */
        List<Entity> targets =
                level.getEntities(
                        ownerPlayer,
                        searchArea,

                        entity ->
                                entity.isAlive()
                                        && entity instanceof Enemy
                );

        /*
         * ========================================================
         * Radius
         * ========================================================
         */

        double radiusWithPadding =
                radius
                        + hitPadding;

        double radiusSquared =
                radiusWithPadding
                        * radiusWithPadding;

        DamageSource damageSource =
                level.damageSources()
                        .playerAttack(
                                ownerPlayer
                        );

        /*
         * ========================================================
         * Damage
         * ========================================================
         */

        for (Entity entity : targets) {

            UUID id =
                    entity.getUUID();

            /*
             * 이번 스킬 발동에서
             * 이미 맞은 엔티티.
             */
            if (hitEntities.contains(id)) {
                continue;
            }

            double distanceSquared =
                    minimumHorizontalDistanceSquared(
                            center,
                            entity.getBoundingBox()
                    );

            if (distanceSquared
                    > radiusSquared) {

                continue;
            }

            /*
             * 먼저 hit 처리.
             *
             * 다음 tick부터는 다시 공격하지 않는다.
             */
            hitEntities.add(id);

            entity.hurtServer(
                    level,
                    damageSource,
                    damage
            );
        }
    }

    /*
     * ============================================================
     * Geometry
     * ============================================================
     */

    private static double minimumHorizontalDistanceSquared(
            Vec3 origin,
            AABB box
    ) {

        /*
         * bounding box가 역순이어도
         * Math.clamp가 터지지 않도록 정규화.
         */

        double minX =
                Math.min(
                        box.minX,
                        box.maxX
                );

        double maxX =
                Math.max(
                        box.minX,
                        box.maxX
                );

        double minZ =
                Math.min(
                        box.minZ,
                        box.maxZ
                );

        double maxZ =
                Math.max(
                        box.minZ,
                        box.maxZ
                );

        double nearestX =
                Math.clamp(
                        origin.x,
                        minX,
                        maxX
                );

        double nearestZ =
                Math.clamp(
                        origin.z,
                        minZ,
                        maxZ
                );

        double dx =
                nearestX
                        - origin.x;

        double dz =
                nearestZ
                        - origin.z;

        return dx * dx
                + dz * dz;
    }
}