package xiaojin.gachaaddiction.data.drawitem;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.world.item.GachaDrawItem;

import java.util.LinkedHashMap;
import java.util.Map;

public class GachaDrawItemDefinitionReloadListener extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "gacha_items";
    private final HolderLookup.Provider registries;

    public GachaDrawItemDefinitionReloadListener(HolderLookup.Provider registries) {
        super(new Gson(), DIRECTORY);
        this.registries = registries;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, GachaDrawItemDefinition> parsed = new LinkedHashMap<>();
        resources.forEach((id, json) -> {
            GachaDrawItemDefinition definition = GachaDrawItemDefinition.CODEC.parse(
                    RegistryOps.create(JsonOps.INSTANCE, registries), json).getOrThrow(message ->
                    new JsonParseException("Invalid draw item " + id + ": " + message));
            definition.item().ifPresent(item -> {
                if (!(item.getItem() instanceof GachaDrawItem)) {
                    throw new JsonParseException("Draw item carrier must be a GachaDrawItem: " + id);
                }
            });
            definition.source().gachaType().ifPresent(type -> {
                if (GachaTypes.get(type) == null) {
                    throw new JsonParseException("Unknown draw item animation: " + type);
                }
            });
            parsed.put(id, definition);
        });
        GachaDrawItemDefinitions.replace(parsed);
    }
}
