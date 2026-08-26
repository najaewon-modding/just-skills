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
    private AmberDiskManager() {}

    private static final double MAX_RADIUS = 10.0;
    private static final int DURATION_TICKS = 20;
    private static final double PARTICLES_PER_BLOCK_AREA = 1.6;
    private static final double DISK_Y_OFFSET_MIN = 0.08;
    private static final double DISK_Y_OFFSET_MAX = 0.32;

    private static final float PARTICLE_ALPHA = 1.0F;
    private static final float PARTICLE_SIZE = 0.90F;
    private static final float PARTICLE_JITTER = 0.075F;
    private static final int PARTICLE_LIFETIME = 20;

    private static final float DAMAGE = 24.0F;
    private static final double VERTICAL_RANGE = 4.0;
    private static final double HIT_PADDING = 0.20;

    private static final List<ActiveDisk> ACTIVE_DISKS = new ArrayList<>();

    public static void spawn(ServerPlayer player) {
        ACTIVE_DISKS.add(new ActiveDisk(player.getUUID(), player.level().dimension(), player.position()));
    }

    public static void tick(ServerLevel level) {
        Iterator<ActiveDisk> iterator = ACTIVE_DISKS.iterator();

        while (iterator.hasNext()) {
            ActiveDisk disk = iterator.next();
            if (!disk.dimension.equals(level.dimension())) continue;

            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(disk.owner);
            if (owner == null || !owner.level().dimension().equals(disk.dimension)) {
                iterator.remove();
                continue;
            }

            if (!disk.tick(level, owner)) iterator.remove();
        }
    }

    private static final class ActiveDisk {
        private final UUID owner;
        private final ResourceKey<Level> dimension;
        private final Vec3 center;
        private final Set<UUID> hitEntities = new HashSet<>();
        private int age = 0;

        private ActiveDisk(UUID owner, ResourceKey<Level> dimension, Vec3 center) {
            this.owner = owner;
            this.dimension = dimension;
            this.center = center;
        }

        private boolean tick(ServerLevel level, ServerPlayer ownerPlayer) {
            double previousProgress = Math.clamp(age / (double) DURATION_TICKS, 0.0, 1.0);
            double previousRadius = MAX_RADIUS * previousProgress;
            age++;
            double currentProgress = Math.clamp(age / (double) DURATION_TICKS, 0.0, 1.0);
            double currentRadius = MAX_RADIUS * currentProgress;

            spawnNewDiskArea(level, previousRadius, currentRadius);
            RadialDamage.damageHostilesOnce(level, ownerPlayer, center, currentRadius, VERTICAL_RANGE, HIT_PADDING, DAMAGE, hitEntities);
            return age < DURATION_TICKS;
        }

        private void spawnNewDiskArea(ServerLevel level, double innerRadius, double outerRadius) {
            if (outerRadius <= innerRadius) return;

            double area = Math.PI * (outerRadius * outerRadius - innerRadius * innerRadius);
            int particleCount = Math.max(2, (int) Math.ceil(area * PARTICLES_PER_BLOCK_AREA));
            RandomSource random = level.getRandom();

            JitterParticleOption darkAmber = particle(0.42F, 0.14F, 0.025F);
            JitterParticleOption amber = particle(0.82F, 0.31F, 0.045F);
            JitterParticleOption brightAmber = particle(1.00F, 0.52F, 0.09F);

            double innerSquared = innerRadius * innerRadius;
            double outerSquared = outerRadius * outerRadius;

            for (int i = 0; i < particleCount; i++) {
                double radius = Math.sqrt(innerSquared + random.nextDouble() * (outerSquared - innerSquared));
                double angle = random.nextDouble() * Math.PI * 2.0;
                double x = center.x + Math.cos(angle) * radius;
                double y = center.y + randomBetween(random, DISK_Y_OFFSET_MIN, DISK_Y_OFFSET_MAX);
                double z = center.z + Math.sin(angle) * radius;
                double colorRoll = random.nextDouble();

                JitterParticleOption selected;
                if (colorRoll < 0.25) selected = darkAmber;
                else if (colorRoll < 0.80) selected = amber;
                else selected = brightAmber;

                level.sendParticles(selected, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        private JitterParticleOption particle(float red, float green, float blue) {
            return new JitterParticleOption(red, green, blue, PARTICLE_ALPHA, PARTICLE_SIZE, PARTICLE_JITTER, PARTICLE_LIFETIME);
        }

        private double randomBetween(RandomSource random, double min, double max) {
            return min + random.nextDouble() * (max - min);
        }
    }
}