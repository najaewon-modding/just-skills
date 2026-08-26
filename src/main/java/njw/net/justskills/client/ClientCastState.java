package njw.net.justskills.client;

public final class ClientCastState {

    private ClientCastState() {
    }

    private static boolean movementLocked = false;

    public static boolean isMovementLocked() {
        return movementLocked;
    }

    public static void setMovementLocked(
            boolean movementLocked
    ) {
        ClientCastState.movementLocked =
                movementLocked;
    }

    public static void reset() {
        movementLocked = false;
    }
}