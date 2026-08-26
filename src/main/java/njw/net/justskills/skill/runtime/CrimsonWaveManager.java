package njw.net.justskills.skill.runtime;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import njw.net.justskills.particle.BallisticParticleOption;
import njw.net.justskills.skill.runtime.util.RadialDamage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CrimsonWaveManager {
    private CrimsonWaveManager() {}

    private static final double MAX_RADIUS = 10.0;
    private static final int DURATION_TICKS = 20;
    private static final double SPIRAL_TURNS = 6.0;
    private static final int TOTAL_EMITTERS = 180;
    private static final int DROPLETS_PER_EMITTER = 4;

    private static final double COLUMN_VERTICAL_INNER_MIN = 0.25;
    private static final double COLUMN_VERTICAL_INNER_MAX = 0.31;
    private static final double COLUMN_VERTICAL_OUTER_MIN = 0.49;
    private static final double COLUMN_VERTICAL_OUTER_MAX = 0.60;
    private static final double SPRAY_VERTICAL_INNER_MIN = 0.18;
    private static final double SPRAY_VERTICAL_INNER_MAX = 0.24;
    private static final double SPRAY_VERTICAL_OUTER_MIN = 0.34;
    private static final double SPRAY_VERTICAL_OUTER_MAX = 0.46;
    private static final double COLUMN_OUTWARD_MIN = 0.015;
    private static final double COLUMN_OUTWARD_MAX = 0.045;
    private static final double SPRAY_OUTWARD_MIN = 0.095;
    private static final double SPRAY_OUTWARD_MAX = 0.155;
    private static final double TANGENTIAL_JITTER = 0.055;

    private static final float PARTICLE_ALPHA = 1.0F;
    private static final float PARTICLE_SIZE = 0.40F;
    private static final int PARTICLE_LIFETIME = 32;
    private static final float PARTICLE_GRAVITY = 0.040F;
    private static final float PARTICLE_DRAG = 0.96F;

    private static final float DAMAGE = 24.0F;
    private static final double VERTICAL_RANGE = 4.0;
    private static final double HIT_PADDING = 0.20;

    private static final List<ActiveWave> ACTIVE_WAVES = new ArrayList<>();

    public static void spawn(ServerPlayer player) {
        ACTIVE_WAVES.add(new ActiveWave(player.getUUID(), player.level().dimension(), player.position()));
    }

    public static void tick(ServerLevel level) {
        Iterator<ActiveWave> iterator = ACTIVE_WAVES.iterator();

        while (iterator.hasNext()) {
            ActiveWave wave = iterator.next();
            if (!wave.dimension.equals(level.dimension())) continue;

            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(wave.owner);
            if (owner == null || !owner.level().dimension().equals(wave.dimension)) {
                iterator.remove();
                continue;
            }

            if (!wave.tick(level, owner)) iterator.remove();
        }
    }

    private static final class ActiveWave {
        private final UUID owner;
        private final ResourceKey<Level> dimension;
        private final Vec3 center;
        private final Set<UUID> hitEntities = new HashSet<>();
        private int age = 0;
        private double currentRadius = 0.0;

        private ActiveWave(UUID owner, ResourceKey<Level> dimension, Vec3 center) {
            this.owner = owner;
            this.dimension = dimension;
            this.center = center;
        }

        private boolean tick(ServerLevel level, ServerPlayer ownerPlayer) {
            int firstEmitter = age * TOTAL_EMITTERS / DURATION_TICKS;
            int endEmitter = (age + 1) * TOTAL_EMITTERS / DURATION_TICKS;

            for (int emitterIndex = firstEmitter; emitterIndex < endEmitter; emitterIndex++) {
                double progress = Math.clamp(emitterIndex / (double) (TOTAL_EMITTERS - 1), 0.0, 1.0);
                double radius = MAX_RADIUS * progress;
                double angle = progress * SPIRAL_TURNS * Math.PI * 2.0;
                currentRadius = Math.max(currentRadius, radius);
                spawnEmitter(level, radius, angle, progress);
            }

            RadialDamage.damageHostilesOnce(level, ownerPlayer, center, currentRadius, VERTICAL_RANGE, HIT_PADDING, DAMAGE, hitEntities);
            age++;
            return age < DURATION_TICKS;
        }

        private void spawnEmitter(ServerLevel level, double radius, double angle, double progress) {
            RandomSource random = level.getRandom();
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = center.x + cos * radius;
            double y = center.y + 0.01;
            double z = center.z + sin * radius;
            double clampedProgress = Math.clamp(progress, 0.0, 1.0);

            double columnVerticalMin = lerp(COLUMN_VERTICAL_INNER_MIN, COLUMN_VERTICAL_OUTER_MIN, clampedProgress);
            double columnVerticalMax = lerp(COLUMN_VERTICAL_INNER_MAX, COLUMN_VERTICAL_OUTER_MAX, clampedProgress);
            double sprayVerticalMin = lerp(SPRAY_VERTICAL_INNER_MIN, SPRAY_VERTICAL_OUTER_MIN, clampedProgress);
            double sprayVerticalMax = lerp(SPRAY_VERTICAL_INNER_MAX, SPRAY_VERTICAL_OUTER_MAX, clampedProgress);

            BallisticParticleOption darkRed = particle(0.55F, 0.005F, 0.010F);
            BallisticParticleOption crimson = particle(0.88F, 0.015F, 0.025F);
            BallisticParticleOption brightRed = particle(1.00F, 0.070F, 0.090F);

            double columnOutward = randomBetween(random, COLUMN_OUTWARD_MIN, COLUMN_OUTWARD_MAX);
            double columnVertical = randomBetween(random, columnVerticalMin, columnVerticalMax);
            double columnTangent = randomBetween(random, -TANGENTIAL_JITTER * 0.35, TANGENTIAL_JITTER * 0.35);
            sendDroplet(level, crimson, x, y, z, cos * columnOutward - sin * columnTangent, columnVertical, sin * columnOutward + cos * columnTangent);

            for (int droplet = 1; droplet < DROPLETS_PER_EMITTER; droplet++) {
                double outward = randomBetween(random, SPRAY_OUTWARD_MIN, SPRAY_OUTWARD_MAX);
                double vertical = randomBetween(random, sprayVerticalMin, sprayVerticalMax);
                double tangent = randomBetween(random, -TANGENTIAL_JITTER, TANGENTIAL_JITTER);
                BallisticParticleOption selected = random.nextBoolean() ? brightRed : darkRed;
                sendDroplet(level, selected, x, y, z, cos * outward - sin * tangent, vertical, sin * outward + cos * tangent);
            }
        }

        private BallisticParticleOption particle(float red, float green, float blue) {
            return new BallisticParticleOption(red, green, blue, PARTICLE_ALPHA, PARTICLE_SIZE, PARTICLE_LIFETIME, PARTICLE_GRAVITY, PARTICLE_DRAG);
        }

        private void sendDroplet(ServerLevel level, BallisticParticleOption particle, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            level.sendParticles(particle, x, y, z, 0, velocityX, velocityY, velocityZ, 1.0);
        }

        private double lerp(double start, double end, double progress) {
            return start + (end - start) * Math.clamp(progress, 0.0, 1.0);
        }

        private double randomBetween(RandomSource random, double min, double max) {
            return min + random.nextDouble() * (max - min);
        }
    }
}