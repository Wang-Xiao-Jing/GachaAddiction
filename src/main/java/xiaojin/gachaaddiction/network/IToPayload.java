package xiaojin.gachaaddiction.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IToPayload extends CustomPacketPayload {
	default void handle(IPayloadContext context) {
		context.enqueueWork(() -> work(context));
	}

	void work(IPayloadContext context);
}
