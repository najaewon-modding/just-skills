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

public final class AzureWaveManager {

    private AzureWaveManager() {
    }

    /*
     * ============================================================
     * Wave
     * ============================================================
     */

    private static final double MAX_RADIUS =
            10.0;

    private static final int TOTAL_RINGS =
            20;

    private static final double RING_RADIUS_STEP =
            MAX_RADIUS
                    / TOTAL_RINGS;

    /*
     * 약 1.4초.
     */
    private static final int WAVE_DURATION_TICKS =
            28;

    /*
     * ============================================================
     * Density
     * ============================================================
     */

    private static final double EMITTER_SPACING =
            1.5;

    private static final int DROPLETS_PER_EMITTER =
            4;

    /*
     * ============================================================
     * Velocity
     * ============================================================
     *
     * 낮게 수평으로 퍼짐.
     */

    private static final double COLUMN_VERTICAL_MIN =
            0.0;

    private static final double COLUMN_VERTICAL_MAX =
            0.015;

    private static final double COLUMN_OUTWARD_MIN =
            0.015;

    private static final double COLUMN_OUTWARD_MAX =
            0.045;

    private static final double SPRAY_VERTICAL_MIN =
            0.0;

    private static final double SPRAY_VERTICAL_MAX =
            0.025;

    private static final double SPRAY_OUTWARD_MIN =
            0.095;

    private static final double SPRAY_OUTWARD_MAX =
            0.155;

    private static final double TANGENTIAL_JITTER =
            0.055;

    /*
     * ============================================================
     * Particle
     * ============================================================
     */

    private static final float PARTICLE_ALPHA =
            1.0F;

    private static final float PARTICLE_SIZE =
            0.38F;

    private static final int PARTICLE_LIFETIME =
            26;

    private static final float PARTICLE_GRAVITY =
            0.0F;

    private static final float PARTICLE_DRAG =
            0.94F;

    /*
     * ============================================================
     * Damage
     * ============================================================
     */

    private static final float DAMAGE =
            24.0F;

    private static final double VERTICAL_RANGE =
            4.0;

    private static final double HIT_PADDING =
            0.20;

    /*
     * ============================================================
     * Active Waves
     * ============================================================
     */

    private static final List<ActiveWave>
            ACTIVE_WAVES =
            new ArrayList<>();

    public static void spawn(
            ServerPlayer player
    ) {

        ACTIVE_WAVES.add(
                new ActiveWave(
                        player.getUUID(),
                        player.level().dimension(),
                        player.position()
                )
        );
    }

    public static void tick(
            ServerLevel level
    ) {

        Iterator<ActiveWave> iterator =
                ACTIVE_WAVES.iterator();

        while (iterator.hasNext()) {

            ActiveWave wave =
                    iterator.next();

            if (!wave.dimension.equals(
                    level.dimension()
            )) {

                continue;
            }

            ServerPlayer owner =
                    level.getServer()
                            .getPlayerList()
                            .getPlayer(
                                    wave.owner
                            );

            if (owner == null) {

                iterator.remove();
                continue;
            }

            if (!owner.level()
                    .dimension()
                    .equals(
                            wave.dimension
                    )) {

                iterator.remove();
                continue;
            }

            if (!wave.tick(
                    level,
                    owner
            )) {

                iterator.remove();
            }
        }
    }

    /*
     * ============================================================
     * Active Wave
     * ============================================================
     */

    private static final class ActiveWave {

        private final UUID owner;

        private final ResourceKey<Level>
                dimension;

        private final Vec3 center;

        private final Set<UUID>
                hitEntities =
                new HashSet<>();

        private int age =
                0;

        private int nextRingIndex =
                1;

        private double currentRadius =
                0.0;

        private ActiveWave(
                UUID owner,
                ResourceKey<Level> dimension,
                Vec3 center
        ) {

            this.owner =
                    owner;

            this.dimension =
                    dimension;

            this.center =
                    center;
        }

        private boolean tick(
                ServerLevel level,
                ServerPlayer ownerPlayer
        ) {

            double progress =
                    Math.clamp(
                            (age + 1)
                                    / (double)
                                    WAVE_DURATION_TICKS,

                            0.0,
                            1.0
                    );

            int targetRingCount =
                    Math.clamp(
                            (int)
                                    Math.ceil(
                                            progress
                                                    * TOTAL_RINGS
                                    ),

                            0,
                            TOTAL_RINGS
                    );

            while (nextRingIndex
                    <= targetRingCount) {

                double radius =
                        Math.clamp(
                                nextRingIndex
                                        * RING_RADIUS_STEP,

                                0.0,
                                MAX_RADIUS
                        );

                currentRadius =
                        radius;

                spawnLiquidRing(
                        level,
                        radius
                );

                nextRingIndex++;
            }

            /*
             * ====================================================
             * Common radial damage
             * ====================================================
             */

            RadialDamage.damageHostilesOnce(
                    level,
                    ownerPlayer,
                    center,
                    currentRadius,
                    VERTICAL_RANGE,
                    HIT_PADDING,
                    DAMAGE,
                    hitEntities
            );

            age++;

            return age
                    < WAVE_DURATION_TICKS;
        }

        /*
         * ========================================================
         * Ring
         * ========================================================
         */

        private void spawnLiquidRing(
                ServerLevel level,
                double radius
        ) {

            RandomSource random =
                    level.getRandom();

            double circumference =
                    Math.PI
                            * 2.0
                            * radius;

            int emitterCount =
                    Math.max(
                            8,

                            (int)
                                    Math.ceil(
                                            circumference
                                                    / EMITTER_SPACING
                                    )
                    );

            BallisticParticleOption darkBlue =
                    particle(
                            0.015F,
                            0.10F,
                            0.55F
                    );

            BallisticParticleOption azure =
                    particle(
                            0.025F,
                            0.38F,
                            0.95F
                    );

            BallisticParticleOption brightBlue =
                    particle(
                            0.20F,
                            0.70F,
                            1.00F
                    );

            double angularOffset =
                    random.nextDouble()
                            * Math.PI
                            * 2.0;

            for (int i = 0;
                 i < emitterCount;
                 i++) {

                double angle =
                        angularOffset
                                + Math.PI
                                * 2.0
                                * i
                                / emitterCount;

                double cos =
                        Math.cos(angle);

                double sin =
                        Math.sin(angle);

                double x =
                        center.x
                                + cos * radius;

                double y =
                        center.y
                                + 0.08;

                double z =
                        center.z
                                + sin * radius;

                /*
                 * =================================================
                 * Center
                 * =================================================
                 */

                double columnOutward =
                        randomBetween(
                                random,
                                COLUMN_OUTWARD_MIN,
                                COLUMN_OUTWARD_MAX
                        );

                double columnVertical =
                        randomBetween(
                                random,
                                COLUMN_VERTICAL_MIN,
                                COLUMN_VERTICAL_MAX
                        );

                double columnTangent =
                        randomBetween(
                                random,
                                -TANGENTIAL_JITTER * 0.35,
                                TANGENTIAL_JITTER * 0.35
                        );

                sendDroplet(
                        level,
                        azure,

                        x,
                        y,
                        z,

                        cos * columnOutward
                                - sin * columnTangent,

                        columnVertical,

                        sin * columnOutward
                                + cos * columnTangent
                );

                /*
                 * =================================================
                 * Spray
                 * =================================================
                 */

                for (int droplet = 1;
                     droplet < DROPLETS_PER_EMITTER;
                     droplet++) {

                    double outward =
                            randomBetween(
                                    random,
                                    SPRAY_OUTWARD_MIN,
                                    SPRAY_OUTWARD_MAX
                            );

                    double vertical =
                            randomBetween(
                                    random,
                                    SPRAY_VERTICAL_MIN,
                                    SPRAY_VERTICAL_MAX
                            );

                    double tangent =
                            randomBetween(
                                    random,
                                    -TANGENTIAL_JITTER,
                                    TANGENTIAL_JITTER
                            );

                    BallisticParticleOption selected =
                            random.nextBoolean()
                                    ? brightBlue
                                    : darkBlue;

                    sendDroplet(
                            level,
                            selected,

                            x,
                            y,
                            z,

                            cos * outward
                                    - sin * tangent,

                            vertical,

                            sin * outward
                                    + cos * tangent
                    );
                }
            }
        }

        private BallisticParticleOption particle(
                float red,
                float green,
                float blue
        ) {

            return new BallisticParticleOption(
                    red,
                    green,
                    blue,

                    PARTICLE_ALPHA,
                    PARTICLE_SIZE,
                    PARTICLE_LIFETIME,
                    PARTICLE_GRAVITY,
                    PARTICLE_DRAG
            );
        }

        private void sendDroplet(
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

        private double randomBetween(
                RandomSource random,
                double min,
                double max
        ) {

            return min
                    + random.nextDouble()
                    * (max - min);
        }
    }
}