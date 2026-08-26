package njw.net.justskills.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class BallisticParticleType
        extends ParticleType<BallisticParticleOption> {

    public BallisticParticleType(
            boolean overrideLimiter
    ) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<BallisticParticleOption> codec() {

        return BallisticParticleOption.CODEC;
    }

    @Override
    public StreamCodec<
            ? super RegistryFriendlyByteBuf,
            BallisticParticleOption
            > streamCodec() {

        return BallisticParticleOption.STREAM_CODEC;
    }
}