package xiaojin.gachaaddiction.registry;

import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.api.IGachaContainerAdapter;

import java.util.ArrayList;
import java.util.List;

public class GachaContainerAdapters {
    private static final List<IGachaContainerAdapter> ADAPTERS = new ArrayList<>();

    public static void register(IGachaContainerAdapter adapter) {
        ADAPTERS.add(adapter);
    }

    public static @Nullable IGachaContainerAdapter find(MenuProvider provider) {
        for (IGachaContainerAdapter adapter : ADAPTERS) {
            if (adapter.supports(provider)) {
                return adapter;
            }
        }

        return null;
    }
}
