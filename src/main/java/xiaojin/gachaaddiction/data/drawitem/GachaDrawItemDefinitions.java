package xiaojin.gachaaddiction.data.drawitem;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GachaDrawItemDefinitions {
    private static Map<ResourceLocation, GachaDrawItemDefinition> definitions = Map.of();

    public static void replace(Map<ResourceLocation, GachaDrawItemDefinition> updated) {
        definitions = Map.copyOf(updated);
    }

    public static Optional<GachaDrawItemDefinition> find(@Nullable ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : definitions.get(id));
    }

    public static Set<ResourceLocation> ids() {
        return definitions.keySet();
    }

    public static void clear() {
        definitions = Map.of();
    }
}
