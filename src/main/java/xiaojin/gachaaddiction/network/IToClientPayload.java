package xiaojin.gachaaddiction.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IToClientPayload extends IToPayload {
	@Override
	default void work(IPayloadContext context) {
		if (context.player().level().isClientSide()) {
			GachaClientPayloadDispatcher.dispatch(this);
		}
	}
}
