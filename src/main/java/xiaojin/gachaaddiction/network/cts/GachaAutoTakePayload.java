package xiaojin.gachaaddiction.network.cts;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.mixed.IAbstractContainerMenu;
import xiaojin.gachaaddiction.network.IToServerPayload;

public record GachaAutoTakePayload(int menuId, int slotId, ItemStack expected) implements IToServerPayload {
    public static final Type<GachaAutoTakePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "auto_take_reward"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaAutoTakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GachaAutoTakePayload::menuId,
            ByteBufCodecs.VAR_INT, GachaAutoTakePayload::slotId,
            ItemStack.STREAM_CODEC, GachaAutoTakePayload::expected,
            GachaAutoTakePayload::new);

    public GachaAutoTakePayload {
        expected = expected.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, ServerPlayer player) {
        if (!GachaAddictionConfig.COMMON.allowAutoTake.get()) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }

        var menu = player.containerMenu;
        if (menu == player.inventoryMenu) {
            return;
        }
        if (menu.containerId != menuId) {
            return;
        }
        if (!menu.stillValid(player)) {
            return;
        }
        if (slotId < 0) {
            return;
        }
        if (slotId >= menu.slots.size()) {
            return;
        }

        var data = IAbstractContainerMenu.of(menu).gachaaddiction$getGachaaData();
        if (data == null) {
            return;
        }
        if (!data.isOpenGachaaUi()) {
            return;
        }

        var slot = menu.slots.get(slotId);
        ItemStack actual = slot.getItem();
        if (slot.container == player.getInventory()) {
            return;
        }
        if (!slot.mayPickup(player)) {
            return;
        }
        if (actual.isEmpty()) {
            return;
        }
        if (expected.isEmpty()) {
            return;
        }
        if (actual.getCount() != expected.getCount()) {
            return;
        }
        if (!ItemStack.isSameItemSameComponents(actual, expected)) {
            return;
        }

        // 只移动服务端当前槽位，不使用客户端物品创建奖励；背包放不下的部分由菜单留在原处。
        menu.quickMoveStack(player, slotId);
        player.getInventory().setChanged();
        menu.broadcastChanges();
    }
}
