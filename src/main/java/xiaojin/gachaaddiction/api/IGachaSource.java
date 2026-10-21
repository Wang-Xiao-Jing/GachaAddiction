package xiaojin.gachaaddiction.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.item.GachaItemDraws;
import xiaojin.gachaaddiction.registry.GachaTypes;

public interface IGachaSource {
    GachaSourceSettings getSettings();

    void setSettings(GachaSourceSettings settings);

    ItemStack getDisplayItem();

    Object getLockTarget();

    default MenuProviderData inspect(MenuProvider provider) {
        MenuProviderData data = MenuProviderData.inspectLegacy(provider);
        return new MenuProviderData(data.lootTable(), getSettings().gachaType()
                .map(id -> !GachaTypes.getDefault(id).isEmpty())
                .orElse(data.isOpenGachaaUi()));
    }

    default void opened(MenuProvider provider) {
        MenuProviderData.openLegacy(provider);
    }

    default boolean beginDraw(ServerPlayer player, InteractionHand hand, GachaType fallback) {
        GachaType type = getSettings().gachaType()
                .map(GachaTypes::getDefault).orElse(fallback);
        return GachaItemDraws.begin(player, hand, type);
    }
}
