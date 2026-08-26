package njw.net.justskills.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import njw.net.justskills.particle.BallisticParticleOption;

public final class BallisticParticle
        extends SingleQuadParticle {

    private final SpriteSet sprites;

    private final Layer layer;

    private final float originalAlpha;

    private final float gravityStrength;

    private final float drag;

    private BallisticParticle(
            BallisticParticleOption options,
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
         *
         * 스킬의 size를 기준으로
         * 약 ±15% variation.
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

        /*
         * ========================================================
         * Physics parameters
         * ========================================================
         */

        this.gravityStrength =
                options.gravity();

        this.drag =
                options.drag();

        /*
         * 서버에서 넘긴 초기 속도.
         */
        this.xd =
                velocityX;

        this.yd =
                velocityY;

        this.zd =
                velocityZ;

        /*
         * 블록 충돌 없이 ballistic 연출만.
         */
        this.hasPhysics =
                false;

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
         * 중력.
         */
        this.yd -=
                gravityStrength;

        /*
         * 이동.
         */
        this.move(
                this.xd,
                this.yd,
                this.zd
        );

        /*
         * 공기 저항.
         */
        this.xd *=
                drag;

        this.yd *=
                drag;

        this.zd *=
                drag;

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
            implements ParticleProvider<BallisticParticleOption> {

        private final SpriteSet sprites;

        public Provider(
                SpriteSet sprites
        ) {

            this.sprites =
                    sprites;
        }

        @Override
        public Particle createParticle(
                BallisticParticleOption options,
                ClientLevel level,

                double x,
                double y,
                double z,

                double velocityX,
                double velocityY,
                double velocityZ,

                RandomSource random
        ) {

            return new BallisticParticle(
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