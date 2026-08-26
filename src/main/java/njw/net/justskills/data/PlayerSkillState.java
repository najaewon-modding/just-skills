package njw.net.justskills.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record PlayerSkillState(
        Optional<Identifier> currentSkill,
        boolean used,
        long nextRollTick
) {

    public static final MapCodec<PlayerSkillState> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(

                    Identifier.CODEC
                            .optionalFieldOf("current_skill")
                            .forGetter(PlayerSkillState::currentSkill),

                    Codec.BOOL
                            .optionalFieldOf("used", false)
                            .forGetter(PlayerSkillState::used),

                    Codec.LONG
                            .optionalFieldOf("next_roll_tick", 0L)
                            .forGetter(PlayerSkillState::nextRollTick)

            ).apply(instance, PlayerSkillState::new));

    public static PlayerSkillState empty() {
        return new PlayerSkillState(
                Optional.empty(),
                false,
                0L
        );
    }

    public PlayerSkillState withSkill(
            Identifier skillId,
            long nextRollTick
    ) {
        return new PlayerSkillState(
                Optional.of(skillId),
                false,
                nextRollTick
        );
    }

    public PlayerSkillState withoutSkill(long nextRollTick) {
        return new PlayerSkillState(
                Optional.empty(),
                false,
                nextRollTick
        );
    }

    public PlayerSkillState markUsed() {
        return new PlayerSkillState(
                currentSkill,
                true,
                nextRollTick
        );
    }
}