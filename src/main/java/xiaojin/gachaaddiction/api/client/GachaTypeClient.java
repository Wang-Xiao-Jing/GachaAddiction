package xiaojin.gachaaddiction.api.client;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.List;

public class GachaTypeClient<T extends GachaType, S extends BasicGachaScreen> {
    private final T gachaType;
    private final CreateScreen<S> createScreenFunction;

    public GachaTypeClient(T gachaType, CreateScreen<S> createScreenFunction) {
        this.gachaType = gachaType;
        this.createScreenFunction = createScreenFunction;
    }

    public S create(Screen oldScreen, List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries) {
        return createScreenFunction.create(oldScreen, lootTableId, itemStackEntries);
    }

    public S open(Screen oldScreen, List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries) {
        S guiScreen = create(oldScreen, lootTableId, itemStackEntries);
        Minecraft.getInstance().setScreen(guiScreen);
        return guiScreen;
    }

    public S open(List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries) {
        return open(Minecraft.getInstance().screen, lootTableId, itemStackEntries);
    }

    public T getGachaType() {
        return gachaType;
    }

    @FunctionalInterface
    public interface CreateScreen<T extends BasicGachaScreen> {
        T create(Screen oldScreen, List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries);

        default T create(Screen oldScreen, @Nullable ResourceLocation lootTableId, ItemStackEntry itemStackEntries) {
            return create(oldScreen, ObjectList.of(lootTableId), ObjectList.of(itemStackEntries));
        }

        default T create(Screen oldScreen, List<@Nullable ResourceLocation> lootTableId, ItemStackEntry itemStackEntries) {
            return create(oldScreen, lootTableId, ObjectList.of(itemStackEntries));
        }

        default T create(Screen oldScreen, @Nullable ResourceLocation lootTableId, List<ItemStackEntry> itemStackEntries) {
            return create(oldScreen, ObjectList.of(lootTableId), itemStackEntries);
        }
    }
}
