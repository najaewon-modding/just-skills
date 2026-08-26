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

public final class EmeraldSpikesManager {
    private EmeraldSpikesManager() {}

    private static final double MAX_RADIUS = 10.0;
    private static final double SPIKE_SPACING = 1.25;
    private static final int SPAWN_WINDOW_TICKS = 14;
    private static final int GROW_TICKS = 3;
    private static final int RETRACT_TICKS = 4;
    private static final int SPIKE_DURATION_TICKS = GROW_TICKS + RETRACT_TICKS;
    private static final int FIELD_DURATION_TICKS = SPAWN_WINDOW_TICKS - 1 + SPIKE_DURATION_TICKS;

    private static final double SPIKE_HEIGHT_MIN = 0.8;
    private static final double SPIKE_HEIGHT_MAX = 1.6;
    private static final double PARTICLE_SPACING = 0.20;

    private static final float BASE_SIZE = 0.18F;
    private static final float TIP_SIZE = 0.05F;
    private static final float PARTICLE_ALPHA = 1.0F;
    private static final float PARTICLE_JITTER = 0.008F;
    private static final int PARTICLE_LIFETIME = 2;

    private static final float DAMAGE = 24.0F;
    private static final double VERTICAL_RANGE = 4.0;
    private static final double HIT_PADDING = 0.20;

    private static final List<ActiveField> ACTIVE_FIELDS = new ArrayList<>();

    public static void spawn(ServerPlayer player) {
        Vec3 center = player.position();
        RandomSource random = player.level().getRandom();
        List<Spike> spikes = createSpikes(center, random);
        ACTIVE_FIELDS.add(new ActiveField(player.getUUID(), player.level().dimension(), center, spikes));
    }

    private static List<Spike> createSpikes(Vec3 center, RandomSource random) {
        List<Spike> spikes = new ArrayList<>();
        spikes.add(new Spike(center.x, center.z, randomBetween(random, SPIKE_HEIGHT_MIN, SPIKE_HEIGHT_MAX), random.nextInt(SPAWN_WINDOW_TICKS)));

        for (double radius = SPIKE_SPACING; radius <= MAX_RADIUS; radius += SPIKE_SPACING) {
            double circumference = Math.PI * 2.0 * radius;
            int spikeCount = Math.max(6, (int) Math.ceil(circumference / SPIKE_SPACING));
            double angularOffset = random.nextDouble() * Math.PI * 2.0;

            for (int i = 0; i < spikeCount; i++) {
                double angle = angularOffset + Math.PI * 2.0 * i / spikeCount;
                double radiusJitter = randomBetween(random, -SPIKE_SPACING * 0.18, SPIKE_SPACING * 0.18);
                double actualRadius = Math.clamp(radius + radiusJitter, 0.0, MAX_RADIUS);
                double x = center.x + Math.cos(angle) * actualRadius;
                double z = center.z + Math.sin(angle) * actualRadius;
                double height = randomBetween(random, SPIKE_HEIGHT_MIN, SPIKE_HEIGHT_MAX);
                int startTick = random.nextInt(SPAWN_WINDOW_TICKS);
                spikes.add(new Spike(x, z, height, startTick));
            }
        }

        return spikes;
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
        private final List<Spike> spikes;
        private final Set<UUID> hitEntities = new HashSet<>();
        private int age = 0;

        private ActiveField(UUID owner, ResourceKey<Level> dimension, Vec3 center, List<Spike> spikes) {
            this.owner = owner;
            this.dimension = dimension;
            this.center = center;
            this.spikes = spikes;
        }

        private boolean tick(ServerLevel level, ServerPlayer ownerPlayer) {
            if (age == 0) {
                RadialDamage.damageHostilesOnce(level, ownerPlayer, center, MAX_RADIUS, VERTICAL_RANGE, HIT_PADDING, DAMAGE, hitEntities);
            }

            for (Spike spike : spikes) {
                int spikeAge = age - spike.startTick;
                if (spikeAge < 0 || spikeAge >= SPIKE_DURATION_TICKS) continue;

                double height = currentHeight(spike.height, spikeAge);
                if (height <= 0.0) continue;
                renderSpike(level, spike, height);
            }

            age++;
            return age < FIELD_DURATION_TICKS;
        }

        private double currentHeight(double maxHeight, int spikeAge) {
            if (spikeAge < GROW_TICKS) {
                double progress = (spikeAge + 1) / (double) GROW_TICKS;
                return maxHeight * progress;
            }

            int retractAge = spikeAge - GROW_TICKS;
            double progress = (retractAge + 1) / (double) RETRACT_TICKS;
            return maxHeight * (1.0 - Math.clamp(progress, 0.0, 1.0));
        }

        private void renderSpike(ServerLevel level, Spike spike, double currentHeight) {
            int particleCount = Math.max(2, (int) Math.ceil(currentHeight / PARTICLE_SPACING));

            for (int i = 0; i < particleCount; i++) {
                double progress = (i + 0.5) / particleCount;
                double y = center.y + 0.03 + currentHeight * progress;
                double taper = progress * progress;
                float size = (float) (BASE_SIZE + (TIP_SIZE - BASE_SIZE) * taper);
                JitterParticleOption particle = particle(progress, size);
                level.sendParticles(particle, spike.x, y, spike.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        private JitterParticleOption particle(double progress, float size) {
            float red;
            float green;
            float blue;

            if (progress < 0.35) {
                red = 0.010F;
                green = 0.30F;
                blue = 0.040F;
            } else if (progress < 0.80) {
                red = 0.020F;
                green = 0.68F;
                blue = 0.080F;
            } else {
                red = 0.14F;
                green = 1.00F;
                blue = 0.20F;
            }

            return new JitterParticleOption(red, green, blue, PARTICLE_ALPHA, size, PARTICLE_JITTER, PARTICLE_LIFETIME);
        }
    }

    private static final class Spike {
        private final double x;
        private final double z;
        private final double height;
        private final int startTick;

        private Spike(double x, double z, double height, int startTick) {
            this.x = x;
            this.z = z;
            this.height = height;
            this.startTick = startTick;
        }
    }

    private static double randomBetween(RandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }
}