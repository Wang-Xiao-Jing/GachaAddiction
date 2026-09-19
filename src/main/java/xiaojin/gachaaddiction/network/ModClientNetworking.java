package xiaojin.gachaaddiction.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;

@Environment(EnvType.CLIENT)
public class ModClientNetworking {
    private ModClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OpenGachaScreenPayload.TYPE,
                (payload, context) -> context.client().execute(() -> handleOpenGachaScreen(payload)));
    }

    private static void handleOpenGachaScreen(OpenGachaScreenPayload payload) {
        GachaType gachaType;
        if (payload.gachaType().isPresent()) {
            if (payload.gachaType().get().isEmpty()) {
                return;
            }
            gachaType = payload.gachaType().get();
        } else {
            gachaType = GachaAddictionConfig.CLIENT.getDefaultGachaaType();
        }

        var client = GachaTypeClients.get(gachaType);
        if (client == null) {
            return;
        }

        var screen = client.open(payload.lootTableIds(), payload.entries());
        NonNullList<ItemStack> items = NonNullList.of(ItemStack.EMPTY, payload.rewards().toArray(ItemStack[]::new));
        screen.updateRewards(items);
    }
}
