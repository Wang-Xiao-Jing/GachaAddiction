package xiaojin.gachaaddiction.network;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IToServerAndClientPayload extends IToPayload {

	@Override
	default void work(IPayloadContext context) {
		Player player = context.player();

		if (player instanceof AbstractClientPlayer abstractClientPlayer) {
			toClient(context, abstractClientPlayer);
		} else if (player instanceof ServerPlayer serverPlayer) {
			toServer(context, serverPlayer);
		}
	}

	void toServer(IPayloadContext context, ServerPlayer player);

	void toClient(IPayloadContext context, AbstractClientPlayer player);
}
