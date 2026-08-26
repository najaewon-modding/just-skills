package njw.net.justskills.skill.runtime;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import njw.net.justskills.particle.BallisticParticleOption;
import njw.net.justskills.particle.JitterParticleOption;
import njw.net.justskills.skill.runtime.util.RadialDamage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class VioletRiftManager {
    private VioletRiftManager() {}

    private static final double MAX_RADIUS = 10.0;
    private static final int SPAWN_WINDOW_TICKS = 10;
    private static final int GROW_TICKS = 3;
    private static final int HOLD_TICKS = 3;
    private static final int BURST_TICK = GROW_TICKS + HOLD_TICKS;
    private static final int FIELD_DURATION_TICKS = SPAWN_WINDOW_TICKS + BURST_TICK + 1;

    private static final double[] RING_RADII = {2.4, 5.3, 8.4};
    private static final int[] RING_COUNTS = {5, 7, 10};
    private static final double RADIUS_JITTER = 0.65;

    private static final int MIN_NODES = 7;
    private static final int MAX_NODES = 10;
    private static final int SUBDIVISIONS = 3;
    private static final double MIN_HEIGHT = 1.4;
    private static final double MAX_HEIGHT = 2.7;
    private static final double MIN_WIDTH = 0.28;
    private static final double MAX_WIDTH = 0.52;

    private static final float EDGE_SIZE = 0.24F;
    private static final float CORE_SIZE = 0.15F;
    private static final float PARTICLE_JITTER = 0.006F;
    private static final int PARTICLE_LIFETIME = 2;

    private static final int BURST_PARTICLES_PER_NODE = 3;
    private static final double BURST_SPEED_MIN = 0.16;
    private static final double BURST_SPEED_MAX = 0.38;
    private static final double BURST_VERTICAL_MIN = -0.015;
    private static final double BURST_VERTICAL_MAX = 0.16;
    private static final float BURST_SIZE_MIN = 0.18F;
    private static final float BURST_SIZE_MAX = 0.30F;
    private static final int BURST_LIFETIME_MIN = 10;
    private static final int BURST_LIFETIME_MAX = 15;
    private static final float BURST_GRAVITY = 0.012F;
    private static final float BURST_DRAG = 0.94F;

    private static final float DAMAGE = 24.0F;
    private static final int DAMAGE_TICK = 3;
    private static final double VERTICAL_RANGE = 4.0;
    private static final double HIT_PADDING = 0.20;

    private static final List<ActiveField> ACTIVE_FIELDS = new ArrayList<>();

    public static void spawn(ServerPlayer player) {
        RandomSource random = player.level().getRandom();
        Vec3 center = player.position();
        List<Rift> rifts = new ArrayList<>();

        for (int ring = 0; ring < RING_RADII.length; ring++) {
            double angularOffset = random.nextDouble() * Math.PI * 2.0;
            int count = RING_COUNTS[ring];

            for (int i = 0; i < count; i++) {
                double angle = angularOffset + Math.PI * 2.0 * i / count;
                double radius = Math.clamp(
                        RING_RADII[ring] + randomBetween(random, -RADIUS_JITTER, RADIUS_JITTER),
                        0.5,
                        MAX_RADIUS - 0.4
                );

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                double orientation = random.nextDouble() * Math.PI * 2.0;
                double height = randomBetween(random, MIN_HEIGHT, MAX_HEIGHT);
                double width = randomBetween(random, MIN_WIDTH, MAX_WIDTH);
                int nodeCount = randomInclusive(random, MIN_NODES, MAX_NODES);
                int startTick = random.nextInt(SPAWN_WINDOW_TICKS);
                double[] jagged = new double[nodeCount];
                double[] depth = new double[nodeCount];

                for (int node = 0; node < nodeCount; node++) {
                    double progress = node / (double) (nodeCount - 1);
                    double envelope = Math.sin(Math.PI * progress);
                    jagged[node] = randomBetween(random, -0.32, 0.32) * envelope;
                    depth[node] = randomBetween(random, -0.12, 0.12) * envelope;
                }

                rifts.add(new Rift(
                        x,
                        z,
                        orientation,
                        height,
                        width,
                        startTick,
                        jagged,
                        depth,
                        random.nextFloat(),
                        random.nextInt(9)
                ));
            }
        }

        ACTIVE_FIELDS.add(new ActiveField(
                player.getUUID(),
                player.level().dimension(),
                center,
                rifts
        ));
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
        private final List<Rift> rifts;
        private final Set<UUID> hitEntities = new HashSet<>();
        private int age = 0;

        private ActiveField(UUID owner, ResourceKey<Level> dimension, Vec3 center, List<Rift> rifts) {
            this.owner = owner;
            this.dimension = dimension;
            this.center = center;
            this.rifts = rifts;
        }

        private boolean tick(ServerLevel level, ServerPlayer ownerPlayer) {
            if (age == DAMAGE_TICK) {
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

            for (Rift rift : rifts) {
                int localAge = age - rift.startTick;

                if (localAge < 0) continue;

                if (localAge < BURST_TICK) {
                    renderRift(level, rift, localAge);
                } else if (localAge == BURST_TICK) {
                    burstRift(level, rift);
                }
            }

            age++;
            return age < FIELD_DURATION_TICKS;
        }

        private void renderRift(ServerLevel level, Rift rift, int localAge) {
            double opening;

            if (localAge < GROW_TICKS) {
                double progress = (localAge + 1.0) / GROW_TICKS;
                opening = 0.04 + 0.96 * progress * progress;
            } else {
                double holdProgress = (localAge - GROW_TICKS) / (double) Math.max(1, HOLD_TICKS - 1);
                opening = 1.0 + Math.sin(holdProgress * Math.PI) * 0.06;
            }

            double dirX = Math.cos(rift.orientation);
            double dirZ = Math.sin(rift.orientation);
            double normalX = -dirZ;
            double normalZ = dirX;
            int pointIndex = 0;

            for (int node = 0; node < rift.jagged.length - 1; node++) {
                for (int sub = 0; sub < SUBDIVISIONS; sub++) {
                    double segmentProgress = sub / (double) SUBDIVISIONS;
                    double verticalProgress = (node + segmentProgress) / (double) (rift.jagged.length - 1);
                    double jagged = lerp(rift.jagged[node], rift.jagged[node + 1], segmentProgress);
                    double depth = lerp(rift.depth[node], rift.depth[node + 1], segmentProgress);
                    double cx = rift.x + dirX * jagged + normalX * depth;
                    double cz = rift.z + dirZ * jagged + normalZ * depth;
                    double y = center.y + 0.10 + rift.height * verticalProgress;
                    double shape = 0.62 + 0.38 * Math.sin(Math.PI * verticalProgress);
                    double halfWidth = rift.width * opening * shape;
                    boolean bright = (pointIndex + age + rift.brightOffset) % 6 == 0;

                    level.sendParticles(
                            edgeParticle(rift, bright),
                            cx + normalX * halfWidth,
                            y,
                            cz + normalZ * halfWidth,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0
                    );

                    level.sendParticles(
                            edgeParticle(rift, bright),
                            cx - normalX * halfWidth,
                            y,
                            cz - normalZ * halfWidth,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0
                    );

                    level.sendParticles(
                            coreParticle(rift, opening),
                            cx,
                            y,
                            cz,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0
                    );

                    pointIndex++;
                }
            }
        }

        private void burstRift(ServerLevel level, Rift rift) {
            RandomSource random = level.getRandom();
            double dirX = Math.cos(rift.orientation);
            double dirZ = Math.sin(rift.orientation);
            double normalX = -dirZ;
            double normalZ = dirX;

            for (int node = 0; node < rift.jagged.length; node++) {
                double verticalProgress = node / (double) (rift.jagged.length - 1);
                double cx = rift.x + dirX * rift.jagged[node] + normalX * rift.depth[node];
                double cz = rift.z + dirZ * rift.jagged[node] + normalZ * rift.depth[node];
                double y = center.y + 0.10 + rift.height * verticalProgress;

                for (int i = 0; i < BURST_PARTICLES_PER_NODE; i++) {
                    double angle = random.nextDouble() * Math.PI * 2.0;
                    double speed = randomBetween(random, BURST_SPEED_MIN, BURST_SPEED_MAX);
                    double velocityX = Math.cos(angle) * speed;
                    double velocityZ = Math.sin(angle) * speed;
                    double velocityY = randomBetween(random, BURST_VERTICAL_MIN, BURST_VERTICAL_MAX);

                    sendBurstParticle(
                            level,
                            burstParticle(random, rift),
                            cx,
                            y,
                            cz,
                            velocityX,
                            velocityY,
                            velocityZ
                    );
                }
            }
        }

        private JitterParticleOption edgeParticle(Rift rift, boolean bright) {
            float red;
            float green;
            float blue;

            if (bright) {
                red = 0.82F;
                green = 0.18F;
                blue = 1.00F;
            } else {
                red = 0.34F + rift.colorVariation * 0.18F;
                green = 0.02F + rift.colorVariation * 0.03F;
                blue = 0.64F + rift.colorVariation * 0.22F;
            }

            return new JitterParticleOption(
                    red,
                    green,
                    blue,
                    bright ? 1.0F : 0.90F,
                    bright ? EDGE_SIZE * 1.14F : EDGE_SIZE,
                    PARTICLE_JITTER,
                    PARTICLE_LIFETIME
            );
        }

        private JitterParticleOption coreParticle(Rift rift, double opening) {
            return new JitterParticleOption(
                    0.72F + rift.colorVariation * 0.16F,
                    0.14F + rift.colorVariation * 0.07F,
                    1.00F,
                    0.82F,
                    (float) (CORE_SIZE * (0.85 + opening * 0.20)),
                    PARTICLE_JITTER,
                    PARTICLE_LIFETIME
            );
        }

        private BallisticParticleOption burstParticle(RandomSource random, Rift rift) {
            boolean bright = random.nextFloat() < 0.35F;
            float red = bright ? 0.90F : 0.38F + rift.colorVariation * 0.22F;
            float green = bright ? 0.28F : 0.02F + rift.colorVariation * 0.05F;
            float blue = bright ? 1.00F : 0.68F + rift.colorVariation * 0.24F;
            float size = (float) randomBetween(random, BURST_SIZE_MIN, BURST_SIZE_MAX);
            int lifetime = randomInclusive(random, BURST_LIFETIME_MIN, BURST_LIFETIME_MAX);

            return new BallisticParticleOption(
                    red,
                    green,
                    blue,
                    1.0F,
                    size,
                    lifetime,
                    BURST_GRAVITY,
                    BURST_DRAG
            );
        }

        private void sendBurstParticle(
                ServerLevel level,
                BallisticParticleOption particle,
                double x,
                double y,
                double z,
                double velocityX,
                double velocityY,
                double velocityZ
        ) {
            level.sendParticles(
                    particle,
                    x,
                    y,
                    z,
                    0,
                    velocityX,
                    velocityY,
                    velocityZ,
                    1.0
            );
        }
    }

    private static final class Rift {
        private final double x;
        private final double z;
        private final double orientation;
        private final double height;
        private final double width;
        private final int startTick;
        private final double[] jagged;
        private final double[] depth;
        private final float colorVariation;
        private final int brightOffset;

        private Rift(
                double x,
                double z,
                double orientation,
                double height,
                double width,
                int startTick,
                double[] jagged,
                double[] depth,
                float colorVariation,
                int brightOffset
        ) {
            this.x = x;
            this.z = z;
            this.orientation = orientation;
            this.height = height;
            this.width = width;
            this.startTick = startTick;
            this.jagged = jagged;
            this.depth = depth;
            this.colorVariation = colorVariation;
            this.brightOffset = brightOffset;
        }
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