package njw.net.justskills.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class JitterParticleType
        extends ParticleType<JitterParticleOption> {

    public JitterParticleType(
            boolean overrideLimiter
    ) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<JitterParticleOption> codec() {

        return JitterParticleOption.CODEC;
    }

    @Override
    public StreamCodec<
            ? super RegistryFriendlyByteBuf,
            JitterParticleOption
            > streamCodec() {

        return JitterParticleOption.STREAM_CODEC;
    }
}