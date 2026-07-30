package xiaojin.gachaaddiction.api.client;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.GachaaData;
import xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.ArrayList;
import java.util.List;

public class GachaTypeClient<T extends GachaType, S extends BasicGachaScreen> {
    private final T gachaType;
    private final CreateScreen<S> createScreenFunction;

    public GachaTypeClient(T gachaType, CreateScreen<S> createScreenFunction) {
        this.gachaType = gachaType;
        this.createScreenFunction = createScreenFunction;
    }

    public S create(Screen oldScreen, GachaaData gachaaData) {
        return create(oldScreen, gachaaData.lootTableId().orElse(new ArrayList<>()), gachaaData.itemStackEntry().orElse(new ArrayList<>()));
    }

    public S create(Screen oldScreen, List<@Nullable Identifier> lootTableId, List<ItemStackEntry> itemStackEntries) {
        return createScreenFunction.create(oldScreen, lootTableId, itemStackEntries);
    }

    public S open(Screen oldScreen, List<@Nullable Identifier> lootTableId, List<ItemStackEntry> itemStackEntries) {
        S guiScreen = create(oldScreen, lootTableId, itemStackEntries);
        Minecraft.getInstance().gui.setScreen(guiScreen);
        return guiScreen;
    }

    public S open(List<@Nullable Identifier> lootTableId, List<ItemStackEntry> itemStackEntries) {
        return open(Minecraft.getInstance().gui.screen(), lootTableId, itemStackEntries);
    }

    public T getGachaType() {
        return gachaType;
    }

    @FunctionalInterface
    public interface CreateScreen<T extends BasicGachaScreen> {
        T create(Screen oldScreen, List<@Nullable Identifier> lootTableId, List<ItemStackEntry> itemStackEntries);

        default T create(Screen oldScreen, @Nullable Identifier lootTableId, ItemStackEntry itemStackEntries) {
            return create(oldScreen, ObjectList.of(lootTableId), ObjectList.of(itemStackEntries));
        }

        default T create(Screen oldScreen, List<@Nullable Identifier> lootTableId, ItemStackEntry itemStackEntries) {
            return create(oldScreen, lootTableId, ObjectList.of(itemStackEntries));
        }

        default T create(Screen oldScreen, @Nullable Identifier lootTableId, List<ItemStackEntry> itemStackEntries) {
            return create(oldScreen, ObjectList.of(lootTableId), itemStackEntries);
        }
    }
}
