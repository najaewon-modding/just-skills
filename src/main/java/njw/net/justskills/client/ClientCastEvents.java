package njw.net.justskills.client;

import net.minecraft.world.entity.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import njw.net.justskills.JustSkills;
import njw.net.justskills.network.CastStatePayload;

@EventBusSubscriber(
        modid = JustSkills.MODID,
        value = Dist.CLIENT
)
public final class ClientCastEvents {

    private ClientCastEvents() {
    }

    /*
     * ============================================================
     * Clientbound network handler
     * ============================================================
     */

    @SubscribeEvent
    private static void registerPayloadHandlers(
            RegisterClientPayloadHandlersEvent event
    ) {

        event.register(
                CastStatePayload.TYPE,
                (payload, context) ->
                        ClientCastState
                                .setMovementLocked(
                                        payload.movementLocked()
                                )
        );
    }

    /*
     * ============================================================
     * Movement input
     * ============================================================
     */

    @SubscribeEvent
    private static void onMovementInput(
            MovementInputUpdateEvent event
    ) {

        if (!ClientCastState.isMovementLocked()) {
            return;
        }

        /*
         * 다음 입력을 모두 false로 만든다:
         *
         * forward
         * backward
         * left
         * right
         * jump
         * shift
         * sprint
         */
        event.getInput().keyPresses =
                Input.EMPTY;

        /*
         * 이미 sprint 상태였다면 즉시 해제.
         */
        event.getEntity()
                .setSprinting(false);
    }

    /*
     * 서버에서 나왔는데 마지막 cast-state 패킷을
     * 못 받은 상황에 대비한다.
     */
    @SubscribeEvent
    private static void onLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {

        ClientCastState.reset();
    }

    /*
     * 새로운 서버 접속 시에도 항상 초기화.
     */
    @SubscribeEvent
    private static void onLogin(
            ClientPlayerNetworkEvent.LoggingIn event
    ) {

        ClientCastState.reset();
    }
}