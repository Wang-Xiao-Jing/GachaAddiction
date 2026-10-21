package xiaojin.gachaaddiction.network;

import java.util.Objects;
import java.util.function.Consumer;

public class GachaClientPayloadDispatcher {
    private static Consumer<IToClientPayload> handler;

    public static void register(Consumer<IToClientPayload> handler) {
        if (GachaClientPayloadDispatcher.handler != null) {
            throw new IllegalStateException("Client payload handler is already registered");
        }

        GachaClientPayloadDispatcher.handler = Objects.requireNonNull(handler);
    }

    public static void dispatch(IToClientPayload payload) {
        if (handler == null) {
            throw new IllegalStateException("Client payload handler is not registered");
        }

        handler.accept(payload);
    }
}
