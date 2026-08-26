package njw.net.justskills.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import njw.net.justskills.JustSkills;

public record UseSkillPayload()
        implements CustomPacketPayload {

    public static final UseSkillPayload INSTANCE =
            new UseSkillPayload();

    public static final Type<UseSkillPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            JustSkills.MODID,
                            "use_skill"
                    )
            );

    public static final StreamCodec<
            ByteBuf,
            UseSkillPayload
            > STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}