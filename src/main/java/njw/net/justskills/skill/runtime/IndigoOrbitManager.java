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

public final class IndigoOrbitManager {
    private IndigoOrbitManager() {}

    private static final double MAX_RADIUS = 10.0;
    private static final double START_RADIUS = 9.5;
    private static final double END_RADIUS = 0.45;
    private static final int DURATION_TICKS = 20;
    private static final int INITIAL_ORBIT_COUNT = 4;
    private static final int SLOW_SPAWN_INTERVAL = 4;
    private static final int FAST_SPAWN_INTERVAL = 2;
    private static final double MIN_INWARD_SPEED = 0.08;
    private static final double MAX_INWARD_SPEED = 1.15;
    private static final double SPEED_CURVE_POWER = 1.6;
    private static final int MIN_POINT_COUNT = 38;
    private static final int MAX_POINT_COUNT = 48;
    private static final double MIN_ANGULAR_SPEED = 0.18;
    private static final double MAX_ANGULAR_SPEED = 0.30;
    private static final double MIN_VERTICAL_OFFSET = 0.10;
    private static final double MAX_VERTICAL_OFFSET = 0.30;
    private static final double MIN_WAVE_AMPLITUDE = 0.03;
    private static final double MAX_WAVE_AMPLITUDE = 0.09;
    private static final float NORMAL_SIZE_OUTER = 0.28F;
    private static final float NORMAL_SIZE_INNER = 0.20F;
    private static final float BRIGHT_SIZE_OUTER = 0.36F;
    private static final float BRIGHT_SIZE_INNER = 0.26F;
    private static final double MAX_SPEED_SIZE_BOOST = 0.15;
    private static final float PARTICLE_JITTER = 0.008F;
    private static final int PARTICLE_LIFETIME = 2;
    private static final float DAMAGE = 24.0F;
    private static final double VERTICAL_RANGE = 4.0;
    private static final double HIT_PADDING = 0.20;
    private static final List<ActiveField> ACTIVE_FIELDS = new ArrayList<>();

    public static void spawn(ServerPlayer player) {
        RandomSource random = player.level().getRandom();
        ActiveField field = new ActiveField(player.getUUID(), player.level().dimension(), player.position());

        for (int i = 0; i < INITIAL_ORBIT_COUNT; i++) {
            double progress = i / (double) (INITIAL_ORBIT_COUNT - 1);
            double radius = lerp(START_RADIUS, 2.4, progress);
            field.orbits.add(createOrbit(random, radius));
        }

        ACTIVE_FIELDS.add(field);
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
        private final List<Orbit> orbits = new ArrayList<>();
        private final Set<UUID> hitEntities = new HashSet<>();
        private int spawnCountdown = SLOW_SPAWN_INTERVAL;
        private int age = 0;

        private ActiveField(UUID owner, ResourceKey<Level> dimension, Vec3 center) {
            this.owner = owner;
            this.dimension = dimension;
            this.center = center;
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

            double timeProgress = Math.clamp(age / (double) (DURATION_TICKS - 1), 0.0, 1.0);
            double speedProgress = Math.pow(timeProgress, SPEED_CURVE_POWER);
            double inwardSpeed = lerp(MIN_INWARD_SPEED, MAX_INWARD_SPEED, speedProgress);

            if (age > 0) {
                spawnCountdown--;

                if (spawnCountdown <= 0) {
                    orbits.add(createOrbit(random, START_RADIUS));
                    spawnCountdown = nextSpawnDelay(random, speedProgress);
                }
            }

            Iterator<Orbit> iterator = orbits.iterator();

            while (iterator.hasNext()) {
                Orbit orbit = iterator.next();

                renderOrbit(level, orbit, speedProgress);

                orbit.radius -= inwardSpeed * orbit.speedMultiplier;
                orbit.age++;

                if (orbit.radius <= END_RADIUS) iterator.remove();
            }

            age++;
            return age < DURATION_TICKS;
        }

        private int nextSpawnDelay(RandomSource random, double speedProgress) {
            int baseDelay = (int) Math.round(lerp(SLOW_SPAWN_INTERVAL, FAST_SPAWN_INTERVAL, speedProgress));
            return baseDelay + random.nextInt(2);
        }

        private void renderOrbit(ServerLevel level, Orbit orbit, double speedProgress) {
            double radiusProgress = 1.0 - Math.clamp(
                    (orbit.radius - END_RADIUS) / (START_RADIUS - END_RADIUS),
                    0.0,
                    1.0
            );

            double rotationMultiplier = lerp(1.0, 1.30, speedProgress);
            double baseAngle = orbit.phaseOffset
                    + orbit.direction
                    * orbit.age
                    * orbit.angularSpeed
                    * rotationMultiplier;

            int brightInterval = Math.max(
                    6,
                    (int) Math.round(lerp(9.0, 6.0, speedProgress))
            );

            for (int i = 0; i < orbit.pointCount; i++) {
                double pointProgress = i / (double) orbit.pointCount;
                double angle = baseAngle + pointProgress * Math.PI * 2.0;
                double x = center.x + Math.cos(angle) * orbit.radius;
                double z = center.z + Math.sin(angle) * orbit.radius;
                double wave = Math.sin(angle * orbit.waveFrequency + orbit.age * 0.16);
                double y = center.y + orbit.verticalOffset + wave * orbit.waveAmplitude;
                boolean bright = (i + orbit.age + orbit.brightOffset) % brightInterval == 0;

                level.sendParticles(
                        createParticle(orbit, radiusProgress, speedProgress, bright),
                        x,
                        y,
                        z,
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0
                );
            }
        }

        private JitterParticleOption createParticle(
                Orbit orbit,
                double radiusProgress,
                double speedProgress,
                boolean bright
        ) {
            float red;
            float green;
            float blue;

            if (bright) {
                red = 0.48F;
                green = 0.27F;
                blue = 0.94F;
            } else {
                red = 0.16F + orbit.colorVariation * 0.12F;
                green = 0.035F + orbit.colorVariation * 0.035F;
                blue = 0.42F + orbit.colorVariation * 0.18F;
            }

            double baseSize = lerp(
                    bright ? BRIGHT_SIZE_OUTER : NORMAL_SIZE_OUTER,
                    bright ? BRIGHT_SIZE_INNER : NORMAL_SIZE_INNER,
                    radiusProgress
            );

            baseSize *= orbit.visualScale;

            float size = (float) (
                    baseSize
                            * (1.0 + MAX_SPEED_SIZE_BOOST * speedProgress)
            );

            float alpha = bright
                    ? 1.0F
                    : (float) lerp(0.88, 0.98, speedProgress);

            return new JitterParticleOption(
                    red,
                    green,
                    blue,
                    alpha,
                    size,
                    PARTICLE_JITTER,
                    PARTICLE_LIFETIME
            );
        }
    }

    private static final class Orbit {
        private double radius;
        private final int pointCount;
        private final double angularSpeed;
        private final int direction;
        private final double phaseOffset;
        private final double verticalOffset;
        private final double waveAmplitude;
        private final double waveFrequency;
        private final double speedMultiplier;
        private final double visualScale;
        private final float colorVariation;
        private final int brightOffset;
        private int age = 0;

        private Orbit(
                double radius,
                int pointCount,
                double angularSpeed,
                int direction,
                double phaseOffset,
                double verticalOffset,
                double waveAmplitude,
                double waveFrequency,
                double speedMultiplier,
                double visualScale,
                float colorVariation,
                int brightOffset
        ) {
            this.radius = radius;
            this.pointCount = pointCount;
            this.angularSpeed = angularSpeed;
            this.direction = direction;
            this.phaseOffset = phaseOffset;
            this.verticalOffset = verticalOffset;
            this.waveAmplitude = waveAmplitude;
            this.waveFrequency = waveFrequency;
            this.speedMultiplier = speedMultiplier;
            this.visualScale = visualScale;
            this.colorVariation = colorVariation;
            this.brightOffset = brightOffset;
        }
    }

    private static Orbit createOrbit(RandomSource random, double radius) {
        return new Orbit(
                radius,
                randomInclusive(random, MIN_POINT_COUNT, MAX_POINT_COUNT),
                randomBetween(random, MIN_ANGULAR_SPEED, MAX_ANGULAR_SPEED),
                random.nextBoolean() ? 1 : -1,
                random.nextDouble() * Math.PI * 2.0,
                randomBetween(random, MIN_VERTICAL_OFFSET, MAX_VERTICAL_OFFSET),
                randomBetween(random, MIN_WAVE_AMPLITUDE, MAX_WAVE_AMPLITUDE),
                randomBetween(random, 2.0, 3.2),
                randomBetween(random, 0.92, 1.08),
                randomBetween(random, 0.90, 1.12),
                random.nextFloat(),
                random.nextInt(9)
        );
    }

    private static int randomInclusive(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static double randomBetween(RandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * Math.clamp(progress, 0.0, 1.0);
    }
}