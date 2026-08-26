package njw.net.justskills.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BallisticParticleOption(
        float red,
        float green,
        float blue,
        float alpha,
        float size,
        int lifetime,
        float gravity,
        float drag
) implements ParticleOptions {

    /*
     * ============================================================
     * Data codec
     * ============================================================
     */

    public static final MapCodec<BallisticParticleOption>
            CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(

                                    Codec.FLOAT
                                            .fieldOf("red")
                                            .forGetter(
                                                    BallisticParticleOption::red
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("green")
                                            .forGetter(
                                                    BallisticParticleOption::green
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("blue")
                                            .forGetter(
                                                    BallisticParticleOption::blue
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("alpha")
                                            .forGetter(
                                                    BallisticParticleOption::alpha
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("size")
                                            .forGetter(
                                                    BallisticParticleOption::size
                                            ),

                                    Codec.INT
                                            .fieldOf("lifetime")
                                            .forGetter(
                                                    BallisticParticleOption::lifetime
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("gravity")
                                            .forGetter(
                                                    BallisticParticleOption::gravity
                                            ),

                                    Codec.FLOAT
                                            .fieldOf("drag")
                                            .forGetter(
                                                    BallisticParticleOption::drag
                                            )

                            ).apply(
                                    instance,
                                    BallisticParticleOption::new
                            )
            );

    /*
     * ============================================================
     * Network codec
     * ============================================================
     */

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            BallisticParticleOption
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public BallisticParticleOption decode(
                        RegistryFriendlyByteBuf buffer
                ) {

                    return new BallisticParticleOption(
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readVarInt(),
                            buffer.readFloat(),
                            buffer.readFloat()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        BallisticParticleOption value
                ) {

                    buffer.writeFloat(value.red());
                    buffer.writeFloat(value.green());
                    buffer.writeFloat(value.blue());
                    buffer.writeFloat(value.alpha());

                    buffer.writeFloat(value.size());

                    buffer.writeVarInt(value.lifetime());

                    buffer.writeFloat(value.gravity());
                    buffer.writeFloat(value.drag());
                }
            };

    /*
     * ============================================================
     * Validation
     * ============================================================
     */

    public BallisticParticleOption {

        red = clamp01(red);
        green = clamp01(green);
        blue = clamp01(blue);
        alpha = clamp01(alpha);

        size =
                Math.max(
                        0.01F,
                        size
                );

        lifetime =
                Math.max(
                        1,
                        lifetime
                );

        gravity =
                Math.max(
                        0.0F,
                        gravity
                );

        drag =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                drag
                        )
                );
    }

    private static float clamp01(
            float value
    ) {

        return Math.clamp(
                value
                ,
                0.0F,
                1.0F);
    }

    @Override
    public ParticleType<?> getType() {

        return ModParticles.BALLISTIC.get();
    }
}