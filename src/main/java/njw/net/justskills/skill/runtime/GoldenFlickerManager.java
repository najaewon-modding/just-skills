package njw.net.justskills.skill.runtime;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import njw.net.justskills.particle.JitterParticleOption;
import njw.net.justskills.skill.runtime.util.RadialDamage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class GoldenFlickerManager {
    private GoldenFlickerManager() {}

    private static final double MAX_RADIUS = 10.0;
    private static final double POINT_SPACING = 1.25;
    private static final int DURATION_TICKS = 20;

    private static final int MIN_ON_TICKS = 1;
    private static final int MAX_ON_TICKS = 4;
    private static final int MIN_OFF_TICKS = 1;
    private static final int MAX_OFF_TICKS = 5;

    private static final float GLOBAL_BLACKOUT_CHANCE = 0.10F;
    private static final float GLOBAL_FLASH_CHANCE = 0.08F;
    private static final float RANDOM_SKIP_CHANCE = 0.12F;

    private static final float PARTICLE_SIZE_MIN = 0.20F;
    private static final float PARTICLE_SIZE_MAX = 0.34F;
    private static final float PARTICLE_ALPHA_MIN = 0.72F;
    private static final float PARTICLE_ALPHA_MAX = 1.0F;
    private static final float PARTICLE_JITTER = 0.012F;
    private static final int PARTICLE_LIFETIME = 2;

    private static final float DAMAGE = 24.0F;
    private static final double VERTICAL_RANGE = 4.0;
    private static final double HIT_PADDING = 0.20;

    private static final List<ActiveField> ACTIVE_FIELDS = new ArrayList<>();

    public static void spawn(ServerPlayer player) {
        Vec3 center = player.position();
        RandomSource random = player.level().getRandom();
        List<FlickerPoint> points = createPoints(center, random);
        ACTIVE_FIELDS.add(new ActiveField(player.getUUID(), player.level().dimension(), center, points));
    }

    private static List<FlickerPoint> createPoints(Vec3 center, RandomSource random) {
        List<FlickerPoint> points = new ArrayList<>();
        points.add(createPoint(center.x, center.z, random));

        for (double radius = POINT_SPACING; radius <= MAX_RADIUS; radius += POINT_SPACING) {
            double circumference = Math.PI * 2.0 * radius;
            int pointCount = Math.max(6, (int) Math.ceil(circumference / POINT_SPACING));
            double angularOffset = random.nextDouble() * Math.PI * 2.0;

            for (int i = 0; i < pointCount; i++) {
                double angle = angularOffset + Math.PI * 2.0 * i / pointCount;
                double radiusJitter = randomBetween(random, -POINT_SPACING * 0.28, POINT_SPACING * 0.28);
                double actualRadius = Math.clamp(radius + radiusJitter, 0.0, MAX_RADIUS);
                double x = center.x + Math.cos(angle) * actualRadius;
                double z = center.z + Math.sin(angle) * actualRadius;
                points.add(createPoint(x, z, random));
            }
        }

        return points;
    }

    private static FlickerPoint createPoint(double x, double z, RandomSource random) {
        double yOffset = randomBetween(random, 0.06, 0.48);
        float size = (float) randomBetween(random, PARTICLE_SIZE_MIN, PARTICLE_SIZE_MAX);
        boolean lit = random.nextBoolean();
        int stateTicksRemaining = lit
                ? randomInclusive(random, MIN_ON_TICKS, MAX_ON_TICKS)
                : randomInclusive(random, MIN_OFF_TICKS, MAX_OFF_TICKS);

        return new FlickerPoint(x, z, yOffset, size, lit, stateTicksRemaining);
    }

    public static void tick(ServerLevel level) {
        Iterator<ActiveField> iterator = ACTIVE_FIELDS.iterator();

        while (iterator.hasNext()) {
            ActiveField field = iterator.next();

            if (!field.dimension.equals(level.dimension())) continue;

            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(field.owner);

            if (owner == null || !owner.level().dimension().equals(field.dimension)) {
                iterator.remove();
                continue;
            }

            if (!field.tick(level, owner)) iterator.remove();
        }
    }

    private static final class ActiveField {
        private final UUID owner;
        private final ResourceKey<Level> dimension;
        private final Vec3 center;
        private final List<FlickerPoint> points;
        private final Set<UUID> hitEntities = new HashSet<>();
        private int age = 0;

        private ActiveField(UUID owner, ResourceKey<Level> dimension, Vec3 center, List<FlickerPoint> points) {
            this.owner = owner;
            this.dimension = dimension;
            this.center = center;
            this.points = points;
        }

        private boolean tick(ServerLevel level, ServerPlayer ownerPlayer) {
            RandomSource random = level.getRandom();

            if (age == 0) {
                RadialDamage.damageHostilesOnce(
                        level,
                        ownerPlayer,
                        center,
                        MAX_RADIUS,
                        VERTICAL_RANGE,
                        HIT_PADDING,
                        DAMAGE,
                        hitEntities
                );
            }

            float globalRoll = random.nextFloat();
            boolean globalBlackout = globalRoll < GLOBAL_BLACKOUT_CHANCE;
            boolean globalFlash = !globalBlackout && globalRoll < GLOBAL_BLACKOUT_CHANCE + GLOBAL_FLASH_CHANCE;

            for (FlickerPoint point : points) {
                point.stateTicksRemaining--;

                if (point.stateTicksRemaining <= 0) {
                    point.lit = !point.lit;
                    point.stateTicksRemaining = point.lit
                            ? randomInclusive(random, MIN_ON_TICKS, MAX_ON_TICKS)
                            : randomInclusive(random, MIN_OFF_TICKS, MAX_OFF_TICKS);
                }

                if (globalBlackout) continue;
                if (!globalFlash && !point.lit) continue;
                if (!globalFlash && random.nextFloat() < RANDOM_SKIP_CHANCE) continue;

                renderPoint(level, point, random, globalFlash);
            }

            age++;
            return age < DURATION_TICKS;
        }

        private void renderPoint(ServerLevel level, FlickerPoint point, RandomSource random, boolean globalFlash) {
            JitterParticleOption particle = createParticle(point, random, globalFlash);
            level.sendParticles(
                    particle,
                    point.x,
                    center.y + point.yOffset,
                    point.z,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }

        private JitterParticleOption createParticle(FlickerPoint point, RandomSource random, boolean globalFlash) {
            float red = 1.0F;
            float green;
            float blue;
            float alpha;
            float sizeMultiplier;

            if (globalFlash) {
                green = 0.96F + random.nextFloat() * 0.04F;
                blue = 0.34F + random.nextFloat() * 0.22F;
                alpha = 1.0F;
                sizeMultiplier = 1.10F + random.nextFloat() * 0.25F;
            } else {
                green = 0.72F + random.nextFloat() * 0.28F;
                blue = 0.02F + random.nextFloat() * 0.25F;
                alpha = PARTICLE_ALPHA_MIN + random.nextFloat() * (PARTICLE_ALPHA_MAX - PARTICLE_ALPHA_MIN);
                sizeMultiplier = 0.78F + random.nextFloat() * 0.44F;
            }

            float size = point.size * sizeMultiplier;
            return new JitterParticleOption(red, green, blue, alpha, size, PARTICLE_JITTER, PARTICLE_LIFETIME);
        }
    }

    private static final class FlickerPoint {
        private final double x;
        private final double z;
        private final double yOffset;
        private final float size;
        private boolean lit;
        private int stateTicksRemaining;

        private FlickerPoint(double x, double z, double yOffset, float size, boolean lit, int stateTicksRemaining) {
            this.x = x;
            this.z = z;
            this.yOffset = yOffset;
            this.size = size;
            this.lit = lit;
            this.stateTicksRemaining = stateTicksRemaining;
        }
    }

    private static int randomInclusive(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static double randomBetween(RandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }
}