package njw.net.justskills.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import njw.net.justskills.JustSkills;

public record CastStatePayload(
        boolean movementLocked
) implements CustomPacketPayload {

    public static final Type<CastStatePayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            JustSkills.MODID,
                            "cast_state"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            CastStatePayload
            > STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    CastStatePayload::movementLocked,
                    CastStatePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}