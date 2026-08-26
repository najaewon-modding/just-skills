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

public final class AmberDiskManager {

    private AmberDiskManager() {
    }

    /*
     * ============================================================
     * Disk
     * ============================================================
     */

    private static final double MAX_RADIUS =
            10.0;

    /*
     * 2초.
     */
    private static final int EXPAND_DURATION_TICKS =
            40;

    /*
     * 단위 면적당 particle 수.
     */
    private static final double PARTICLES_PER_BLOCK_AREA =
            1.6;

    private static final double DISK_Y_OFFSET_MIN =
            0.08;

    private static final double DISK_Y_OFFSET_MAX =
            0.32;

    /*
     * ============================================================
     * Particle
     * ============================================================
     */

    private static final float PARTICLE_ALPHA =
            1.0F;

    /*
     * Amber는 크게.
     */
    private static final float PARTICLE_SIZE =
            0.90F;

    /*
     * 제자리 떨림 범위.
     */
    private static final float PARTICLE_JITTER =
            0.075F;

    /*
     * 약 2.5초.
     */
    private static final int PARTICLE_LIFETIME =
            50;

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
     * Active Disks
     * ============================================================
     */

    private static final List<ActiveDisk>
            ACTIVE_DISKS =
            new ArrayList<>();

    public static void spawn(
            ServerPlayer player
    ) {

        ACTIVE_DISKS.add(
                new ActiveDisk(
                        player.getUUID(),
                        player.level().dimension(),
                        player.position()
                )
        );
    }

    public static void tick(
            ServerLevel level
    ) {

        Iterator<ActiveDisk> iterator =
                ACTIVE_DISKS.iterator();

        while (iterator.hasNext()) {

            ActiveDisk disk =
                    iterator.next();

            if (!disk.dimension.equals(
                    level.dimension()
            )) {

                continue;
            }

            ServerPlayer owner =
                    level.getServer()
                            .getPlayerList()
                            .getPlayer(
                                    disk.owner
                            );

            if (owner == null) {

                iterator.remove();
                continue;
            }

            if (!owner.level()
                    .dimension()
                    .equals(
                            disk.dimension
                    )) {

                iterator.remove();
                continue;
            }

            if (!disk.tick(
                    level,
                    owner
            )) {

                iterator.remove();
            }
        }
    }

    /*
     * ============================================================
     * Active Disk
     * ============================================================
     */

    private static final class ActiveDisk {

        private final UUID owner;

        private final ResourceKey<Level>
                dimension;

        private final Vec3 center;

        private final Set<UUID>
                hitEntities =
                new HashSet<>();

        private int age =
                0;

        private ActiveDisk(
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

            double previousProgress =
                    Math.clamp(
                            age
                                    / (double)
                                    EXPAND_DURATION_TICKS,

                            0.0,
                            1.0
                    );

            double previousRadius =
                    MAX_RADIUS
                            * previousProgress;

            age++;

            double currentProgress =
                    Math.clamp(
                            age
                                    / (double)
                                    EXPAND_DURATION_TICKS,

                            0.0,
                            1.0
                    );

            double currentRadius =
                    MAX_RADIUS
                            * currentProgress;

            /*
             * 새롭게 넓어진 원환에만
             * particle 생성.
             */
            spawnNewDiskArea(
                    level,
                    previousRadius,
                    currentRadius
            );

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

            return age
                    < EXPAND_DURATION_TICKS;
        }

        /*
         * ========================================================
         * Disk particle
         * ========================================================
         */

        private void spawnNewDiskArea(
                ServerLevel level,
                double innerRadius,
                double outerRadius
        ) {

            if (outerRadius
                    <= innerRadius) {

                return;
            }

            /*
             * annulus area:
             *
             * π(R²-r²)
             */
            double area =
                    Math.PI
                            * (
                            outerRadius
                                    * outerRadius
                                    - innerRadius
                                    * innerRadius
                    );

            int particleCount =
                    Math.max(
                            2,

                            (int)
                                    Math.ceil(
                                            area
                                                    * PARTICLES_PER_BLOCK_AREA
                                    )
                    );

            RandomSource random =
                    level.getRandom();

            /*
             * ====================================================
             * Colors
             * ====================================================
             */

            JitterParticleOption darkAmber =
                    particle(
                            0.42F,
                            0.14F,
                            0.025F
                    );

            JitterParticleOption amber =
                    particle(
                            0.82F,
                            0.31F,
                            0.045F
                    );

            JitterParticleOption brightAmber =
                    particle(
                            1.00F,
                            0.52F,
                            0.09F
                    );

            double innerSquared =
                    innerRadius
                            * innerRadius;

            double outerSquared =
                    outerRadius
                            * outerRadius;

            for (int i = 0;
                 i < particleCount;
                 i++) {

                /*
                 * 면적 기준 균일 sampling.
                 */
                double radius =
                        Math.sqrt(
                                innerSquared
                                        + random.nextDouble()
                                        * (
                                        outerSquared
                                                - innerSquared
                                )
                        );

                double angle =
                        random.nextDouble()
                                * Math.PI
                                * 2.0;

                double x =
                        center.x
                                + Math.cos(angle)
                                * radius;

                double y =
                        center.y
                                + randomBetween(
                                random,
                                DISK_Y_OFFSET_MIN,
                                DISK_Y_OFFSET_MAX
                        );

                double z =
                        center.z
                                + Math.sin(angle)
                                * radius;

                double colorRoll =
                        random.nextDouble();

                JitterParticleOption selected;

                if (colorRoll < 0.25) {

                    selected =
                            darkAmber;

                } else if (colorRoll < 0.80) {

                    selected =
                            amber;

                } else {

                    selected =
                            brightAmber;
                }

                level.sendParticles(
                        selected,

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

        /*
         * ========================================================
         * Particle Option
         * ========================================================
         */

        private JitterParticleOption particle(
                float red,
                float green,
                float blue
        ) {

            return new JitterParticleOption(
                    red,
                    green,
                    blue,

                    PARTICLE_ALPHA,
                    PARTICLE_SIZE,
                    PARTICLE_JITTER,
                    PARTICLE_LIFETIME
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