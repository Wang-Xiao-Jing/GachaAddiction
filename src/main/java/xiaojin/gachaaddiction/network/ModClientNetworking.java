package xiaojin.gachaaddiction.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.GachaaData;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.registry.client.GachaTypeClients;
import xiaojin.gachaaddiction.GachaAddiction;

@Environment(EnvType.CLIENT)
public class ModClientNetworking {
    private ModClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(OpenGachaScreenPayload.TYPE,
                (payload, context) -> context.client().execute(() -> handleOpenGachaScreen(payload)));

        ClientPlayNetworking.registerGlobalReceiver(GachaaDataPayload.TYPE,
                (payload, context) -> context.client().execute(() -> handleGachaaData(context.client(), payload)));
    }

    private static void handleGachaaData(Minecraft client, GachaaDataPayload payload) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || menu.containerId != payload.containerId()) {
            return;
        }

        if (payload.data().isEmpty()) {
            return;
        }

        GachaaData data = payload.data().get();
        IAbstractContainerMenu.of(menu).gachaaddiction$setGachaaData(data);

        if (!data.isOpenGachaaUi()) {
            return;
        }

        GachaType gachaType = data.getGachaType();
        if (gachaType.isEmpty()) {
            return;
        }

        Screen current = client.screen;
        BasicGachaScreen gachaScreen = gachaType.getClientData().create(current, data);
        client.setScreen(gachaScreen);

        GachaAddiction.LOGGER.info("[gacha] payload containerId={} lootTables={} entries={}",
                payload.containerId(),
                data.lootTableId().map(java.util.List::size).orElse(-1),
                data.itemStackEntry().map(java.util.List::size).orElse(-1));

        fillRewards(client, gachaScreen, menu);
    }

    private static void fillRewards(Minecraft client, BasicGachaScreen screen, AbstractContainerMenu menu) {
        NonNullList<ItemStack> menuItems = menu.getItems();
        LocalPlayer localPlayer = client.player;
        if (localPlayer != null) {
            menuItems.removeAll(localPlayer.inventoryMenu.getItems());
        }
        menuItems.removeIf(ItemStack::isEmpty);

        GachaAddiction.LOGGER.info("[gacha] fillRewards items={}", menuItems.size());

        if (menuItems.isEmpty()) {
            return;
        }

        if (menuItems.stream().noneMatch(GachaAddictionConfig.CLIENT::filter)) {
            screen.onClose();
            return;
        }

        screen.updateRewards(menuItems);
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
