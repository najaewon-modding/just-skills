package njw.net.justskills.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record JitterParticleOption(
        float red,
        float green,
        float blue,
        float alpha,
        float size,
        float jitterStrength,
        int lifetime
) implements ParticleOptions {

    /*
     * ============================================================
     * Data codec
     * ============================================================
     */

    public static final MapCodec<JitterParticleOption>
            CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(

                                    Codec.FLOAT
                                            .fieldOf("red")
                                            .forGetter(
                                                    JitterParticleOption::red
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("green")
                                            .forGetter(
                                                    JitterParticleOption::green
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("blue")
                                            .forGetter(
                                                    JitterParticleOption::blue
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("alpha")
                                            .forGetter(
                                                    JitterParticleOption::alpha
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("size")
                                            .forGetter(
                                                    JitterParticleOption::size
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("jitter_strength")
                                            .forGetter(
                                                    JitterParticleOption::jitterStrength
                                            ),

                                    Codec.INT
                                            .fieldOf("lifetime")
                                            .forGetter(
                                                    JitterParticleOption::lifetime
                                            )

                            ).apply(
                                    instance,
                                    JitterParticleOption::new
                            )
            );

    /*
     * ============================================================
     * Network codec
     * ============================================================
     */

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            JitterParticleOption
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public JitterParticleOption decode(
                        RegistryFriendlyByteBuf buffer
                ) {

                    return new JitterParticleOption(
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        JitterParticleOption value
                ) {

                    buffer.writeFloat(value.red());
                    buffer.writeFloat(value.green());
                    buffer.writeFloat(value.blue());
                    buffer.writeFloat(value.alpha());

                    buffer.writeFloat(value.size());
                    buffer.writeFloat(value.jitterStrength());

                    buffer.writeVarInt(value.lifetime());
                }
            };

    /*
     * ============================================================
     * Validation
     * ============================================================
     */

    public JitterParticleOption {

        red = clamp01(red);
        green = clamp01(green);
        blue = clamp01(blue);
        alpha = clamp01(alpha);

        size =
                Math.max(
                        0.01F,
                        size
                );

        jitterStrength =
                Math.max(
                        0.0F,
                        jitterStrength
                );

        lifetime =
                Math.max(
                        1,
                        lifetime
                );
    }

    private static float clamp01(
            float value
    ) {

        return Math.max(
                0.0F,
                Math.min(
                        1.0F,
                        value
                )
        );
    }

    @Override
    public ParticleType<?> getType() {

        return ModParticles.JITTER.get();
    }
}