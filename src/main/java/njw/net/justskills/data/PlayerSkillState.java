package njw.net.justskills.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record PlayerSkillState(
        Optional<Identifier> currentSkill,
        long cooldownEndTick
) {

    /*
     * ============================================================
     * Save codec
     * ============================================================
     */

    public static final MapCodec<PlayerSkillState> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(

                                    Identifier.CODEC
                                            .optionalFieldOf(
                                                    "current_skill"
                                            )
                                            .forGetter(
                                                    PlayerSkillState::currentSkill
                                            ),

                                    Codec.LONG
                                            .optionalFieldOf(
                                                    "cooldown_end_tick",
                                                    0L
                                            )
                                            .forGetter(
                                                    PlayerSkillState::cooldownEndTick
                                            )

                            ).apply(
                                    instance,
                                    PlayerSkillState::new
                            )
            );

    /*
     * ============================================================
     * Network codec
     * ============================================================
     */

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            PlayerSkillState
            > STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public PlayerSkillState decode(
                        RegistryFriendlyByteBuf buffer
                ) {

                    Optional<Identifier> currentSkill;

                    if (buffer.readBoolean()) {

                        currentSkill =
                                Optional.of(
                                        Identifier.STREAM_CODEC
                                                .decode(buffer)
                                );

                    } else {

                        currentSkill =
                                Optional.empty();
                    }

                    long cooldownEndTick =
                            buffer.readLong();

                    return new PlayerSkillState(
                            currentSkill,
                            cooldownEndTick
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        PlayerSkillState value
                ) {

                    buffer.writeBoolean(
                            value.currentSkill()
                                    .isPresent()
                    );

                    if (value.currentSkill()
                            .isPresent()) {

                        Identifier.STREAM_CODEC
                                .encode(
                                        buffer,
                                        value.currentSkill()
                                                .get()
                                );
                    }

                    buffer.writeLong(
                            value.cooldownEndTick()
                    );
                }
            };

    /*
     * ============================================================
     * States
     * ============================================================
     */

    public static PlayerSkillState empty() {

        return new PlayerSkillState(
                Optional.empty(),
                0L
        );
    }

    /*
     * 새 스킬을 배정.
     *
     * 현재는 쿨다운 상태가 아니므로
     * cooldownEndTick = 0.
     */
    public PlayerSkillState withSkill(
            Identifier skillId
    ) {

        return new PlayerSkillState(
                Optional.of(skillId),
                0L
        );
    }

    /*
     * 성공적으로 스킬을 사용한 순간:
     *
     * currentSkill 제거
     * cooldown 시작.
     */
    public PlayerSkillState startCooldown(
            long cooldownEndTick
    ) {

        return new PlayerSkillState(
                Optional.empty(),
                cooldownEndTick
        );
    }

    public boolean isCoolingDown(
            long currentTick
    ) {

        return currentSkill.isEmpty()
                && currentTick < cooldownEndTick;
    }

    public boolean canRollSkill(
            long currentTick
    ) {

        return currentSkill.isEmpty()
                && currentTick >= cooldownEndTick;
    }
}