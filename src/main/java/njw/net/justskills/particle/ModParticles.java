package njw.net.justskills.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import njw.net.justskills.JustSkills;

public final class ModParticles {

    private ModParticles() {
    }

    public static final DeferredRegister<ParticleType<?>>
            PARTICLE_TYPES =
            DeferredRegister.create(
                    BuiltInRegistries.PARTICLE_TYPE,
                    JustSkills.MODID
            );

    /*
     * ============================================================
     * Ballistic
     * ============================================================
     */

    public static final DeferredHolder<
            ParticleType<?>,
            BallisticParticleType
            > BALLISTIC =
            PARTICLE_TYPES.register(
                    "ballistic",
                    () ->
                            new BallisticParticleType(
                                    false
                            )
            );

    /*
     * ============================================================
     * Jitter
     * ============================================================
     */

    public static final DeferredHolder<
            ParticleType<?>,
            JitterParticleType
            > JITTER =
            PARTICLE_TYPES.register(
                    "jitter",
                    () ->
                            new JitterParticleType(
                                    false
                            )
            );

    public static void register(
            IEventBus eventBus
    ) {

        PARTICLE_TYPES.register(
                eventBus
        );
    }
}