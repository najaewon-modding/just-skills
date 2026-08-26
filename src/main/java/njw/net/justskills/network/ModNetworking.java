package njw.net.justskills.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import njw.net.justskills.skill.SkillManager;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {

        PayloadRegistrar registrar =
                event.registrar("1");

        /*
         * Client -> Server
         *
         * G 키를 눌렀다는 요청.
         */
        registrar.playToServer(
                UseSkillPayload.TYPE,
                UseSkillPayload.STREAM_CODEC,
                (payload, context) -> {

                    if (context.player()
                            instanceof ServerPlayer player) {

                        SkillManager.tryUseCurrentSkill(
                                player
                        );
                    }
                }
        );

        /*
         * Server -> Client
         *
         * 현재 시전으로 인해 이동이 봉쇄되어 있는지.
         *
         * 실제 client handler는
         * RegisterClientPayloadHandlersEvent에서 등록한다.
         */
        registrar.playToClient(
                CastStatePayload.TYPE,
                CastStatePayload.STREAM_CODEC
        );
    }
}