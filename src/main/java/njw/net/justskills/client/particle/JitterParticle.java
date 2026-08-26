package njw.net.justskills.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import njw.net.justskills.particle.JitterParticleOption;

public final class JitterParticle
        extends SingleQuadParticle {

    private final double originX;
    private final double originY;
    private final double originZ;

    private final float jitterStrength;

    private final float originalAlpha;

    private final SpriteSet sprites;

    private final Layer layer;

    private JitterParticle(
            JitterParticleOption options,
            ClientLevel level,

            double x,
            double y,
            double z,

            double velocityX,
            double velocityY,
            double velocityZ,

            RandomSource random,
            SpriteSet sprites
    ) {

        super(
                level,
                x,
                y,
                z,
                sprites.first()
        );

        /*
         * ========================================================
         * Anchor
         * ========================================================
         */

        this.originX =
                x;

        this.originY =
                y;

        this.originZ =
                z;

        this.sprites =
                sprites;

        this.layer =
                Layer.bySprite(
                        sprites.first()
                );

        /*
         * ========================================================
         * Color
         * ========================================================
         */

        this.setColor(
                options.red(),
                options.green(),
                options.blue()
        );

        this.originalAlpha =
                options.alpha();

        this.setAlpha(
                originalAlpha
        );

        /*
         * ========================================================
         * Size
         * ========================================================
         */

        float sizeVariation =
                0.85F
                        + random.nextFloat()
                        * 0.30F;

        this.quadSize =
                options.size()
                        * sizeVariation;

        /*
         * ========================================================
         * Jitter
         * ========================================================
         */

        this.jitterStrength =
                options.jitterStrength();

        /*
         * ========================================================
         * Lifetime
         * ========================================================
         */

        float lifetimeVariation =
                0.85F
                        + random.nextFloat()
                        * 0.30F;

        this.lifetime =
                Math.max(
                        1,
                        Math.round(
                                options.lifetime()
                                        * lifetimeVariation
                        )
                );

        this.hasPhysics =
                false;

        this.xd =
                0.0;

        this.yd =
                0.0;

        this.zd =
                0.0;

        this.setSpriteFromAge(
                sprites
        );
    }

    /*
     * ============================================================
     * Tick
     * ============================================================
     */

    @Override
    public void tick() {

        this.xo =
                this.x;

        this.yo =
                this.y;

        this.zo =
                this.z;

        if (this.age++
                >= this.lifetime) {

            this.remove();

            return;
        }

        /*
         * ========================================================
         * Jitter
         * ========================================================
         *
         * random walk가 아니라 항상 원래 위치 기준.
         */

        double offsetX =
                (
                        this.random.nextDouble()
                                * 2.0
                                - 1.0
                )
                        * jitterStrength;

        double offsetY =
                (
                        this.random.nextDouble()
                                * 2.0
                                - 1.0
                )
                        * jitterStrength
                        * 0.70;

        double offsetZ =
                (
                        this.random.nextDouble()
                                * 2.0
                                - 1.0
                )
                        * jitterStrength;

        this.setPos(
                originX + offsetX,
                originY + offsetY,
                originZ + offsetZ
        );

        /*
         * ========================================================
         * Fade
         * ========================================================
         */

        float progress =
                this.age
                        / (float)
                        this.lifetime;

        if (progress > 0.65F) {

            float fadeProgress =
                    (
                            progress
                                    - 0.65F
                    )
                            / 0.35F;

            this.setAlpha(
                    Math.max(
                            0.0F,

                            originalAlpha
                                    * (
                                    1.0F
                                            - fadeProgress
                            )
                    )
            );
        }

        this.setSpriteFromAge(
                sprites
        );
    }

    @Override
    protected Layer getLayer() {

        return layer;
    }

    /*
     * ============================================================
     * Provider
     * ============================================================
     */

    public static final class Provider
            implements ParticleProvider<JitterParticleOption> {

        private final SpriteSet sprites;

        public Provider(
                SpriteSet sprites
        ) {

            this.sprites =
                    sprites;
        }

        @Override
        public Particle createParticle(
                JitterParticleOption options,
                ClientLevel level,

                double x,
                double y,
                double z,

                double velocityX,
                double velocityY,
                double velocityZ,

                RandomSource random
        ) {

            return new JitterParticle(
                    options,
                    level,

                    x,
                    y,
                    z,

                    velocityX,
                    velocityY,
                    velocityZ,

                    random,
                    sprites
            );
        }
    }
}