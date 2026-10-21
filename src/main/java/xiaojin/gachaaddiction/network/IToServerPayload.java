package xiaojin.gachaaddiction.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IToServerPayload extends IToPayload {
	void work(IPayloadContext context, ServerPlayer player);

	@Override
	default void work(IPayloadContext context) {
		Player player = context.player();

		if (player instanceof ServerPlayer serverPlayer) {
			work(context, serverPlayer);
		}
	}
}
