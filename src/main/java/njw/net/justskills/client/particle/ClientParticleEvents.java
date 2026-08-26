package njw.net.justskills.client.particle;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import njw.net.justskills.JustSkills;
import njw.net.justskills.particle.ModParticles;

@EventBusSubscriber(
        modid = JustSkills.MODID,
        value = Dist.CLIENT
)
public final class ClientParticleEvents {

    private ClientParticleEvents() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(
            RegisterParticleProvidersEvent event
    ) {

        event.registerSpriteSet(
                ModParticles.BALLISTIC.get(),
                BallisticParticle.Provider::new
        );

        event.registerSpriteSet(
                ModParticles.JITTER.get(),
                JitterParticle.Provider::new
        );
    }
}