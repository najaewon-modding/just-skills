package njw.net.justskills.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class ColoredBallisticParticleType
        extends ParticleType<ColorParticleOption> {

    public ColoredBallisticParticleType(
            boolean overrideLimiter
    ) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<ColorParticleOption> codec() {
        return ColorParticleOption.codec(this);
    }

    @Override
    public StreamCodec<
            ? super RegistryFriendlyByteBuf,
            ColorParticleOption
            > streamCodec() {

        return ColorParticleOption.streamCodec(this);
    }
}