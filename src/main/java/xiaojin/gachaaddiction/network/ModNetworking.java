package xiaojin.gachaaddiction.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ModNetworking {
    private ModNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenGachaScreenPayload.TYPE, OpenGachaScreenPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(GachaaDataPayload.TYPE, GachaaDataPayload.STREAM_CODEC);
    }
}
