package xiaojin.gachaaddiction.network;

import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IToClientPayload extends IToPayload {
	void work(IPayloadContext context, AbstractClientPlayer player);

	@Override
	default void work(IPayloadContext context) {
		var player = context.player();

		if (player instanceof AbstractClientPlayer abstractClientPlayer) {
			work(context, abstractClientPlayer);
		}
	}
}
