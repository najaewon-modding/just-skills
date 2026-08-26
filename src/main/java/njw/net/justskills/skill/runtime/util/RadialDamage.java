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
    private RadialDamage() {}

    public static void damageHostilesOnce(ServerLevel level, ServerPlayer ownerPlayer, Vec3 center, double radius, double verticalRange, double hitPadding, float damage, Set<UUID> hitEntities) {
        if (radius <= 0.0) return;

        AABB searchArea = new AABB(
                center.x - radius - hitPadding,
                center.y - verticalRange,
                center.z - radius - hitPadding,
                center.x + radius + hitPadding,
                center.y + verticalRange,
                center.z + radius + hitPadding
        );

        List<Entity> targets = level.getEntities(ownerPlayer, searchArea, entity -> entity.isAlive() && entity instanceof Enemy);
        double radiusWithPadding = radius + hitPadding;
        double radiusSquared = radiusWithPadding * radiusWithPadding;
        DamageSource damageSource = level.damageSources().playerAttack(ownerPlayer);

        for (Entity entity : targets) {
            UUID id = entity.getUUID();

            if (hitEntities.contains(id)) continue;
            if (minimumHorizontalDistanceSquared(center, entity.getBoundingBox()) > radiusSquared) continue;

            if (entity.hurtServer(level, damageSource, damage)) hitEntities.add(id);
        }
    }

    private static double minimumHorizontalDistanceSquared(Vec3 origin, AABB box) {
        double minX = Math.min(box.minX, box.maxX);
        double maxX = Math.max(box.minX, box.maxX);
        double minZ = Math.min(box.minZ, box.maxZ);
        double maxZ = Math.max(box.minZ, box.maxZ);
        double nearestX = Math.clamp(origin.x, minX, maxX);
        double nearestZ = Math.clamp(origin.z, minZ, maxZ);
        double dx = nearestX - origin.x;
        double dz = nearestZ - origin.z;
        return dx * dx + dz * dz;
    }
}