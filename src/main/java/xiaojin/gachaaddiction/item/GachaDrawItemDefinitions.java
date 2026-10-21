package xiaojin.gachaaddiction.item;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GachaDrawItemDefinitions extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "gacha_items";
    public static final String DEFINITION_KEY = "draw_item";
    private static final String CARD_POOL_KEY = "card_pool";
    private static final String PACKAGE_KEY = "package";
    private static final String PREVIEW_KEY = "preview_items";
    private static Map<ResourceLocation, GachaDrawItemDefinition> definitions = Map.of();
    private final HolderLookup.Provider registries;

    public GachaDrawItemDefinitions(HolderLookup.Provider registries) {
        super(new Gson(), DIRECTORY);
        this.registries = registries;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
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
        definitions = Map.copyOf(parsed);
    }

    public static Optional<GachaDrawItemDefinition> find(ItemStack stack) {
        ResourceLocation id = ResourceLocation.tryParse(binding(stack).getString(DEFINITION_KEY));
        return Optional.ofNullable(id == null ? null : definitions.get(id));
    }

    public static CompoundTag binding(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(GachaAddiction.MODID);
    }

    public static CompoundTag resolveBinding(ItemStack stack) {
        CompoundTag binding = binding(stack);
        if (!binding.contains(DEFINITION_KEY)) {
            return binding;
        }

        ResourceLocation id = ResourceLocation.tryParse(binding.getString(DEFINITION_KEY));
        GachaDrawItemDefinition definition = id == null ? null : definitions.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown draw item definition: " + binding.getString(DEFINITION_KEY));
        }

        binding.putString(CARD_POOL_KEY, definition.cardPool().toString());
        binding.putString(PACKAGE_KEY, definition.drawPackage().toString());
        ListTag previews = new ListTag();
        definition.previewItems().forEach(item -> previews.add(StringTag.valueOf(item.toString())));
        binding.put(PREVIEW_KEY, previews);
        return binding;
    }

    public static ItemStack create(String definitionId) {
        ResourceLocation id = ResourceLocation.parse(definitionId);
        GachaDrawItemDefinition definition = definitions.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown draw item definition: " + id);
        }

        ItemStack stack = definition.item().map(ItemStack::copy).orElseGet(GachaDrawItems.WISH_TOTEM::toStack);
        stack.setCount(1);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag data = root.getCompound(GachaAddiction.MODID);
            data.putString(DEFINITION_KEY, id.toString());
            root.put(GachaAddiction.MODID, data);
        });
        var source = stack.getCapability(GachaSources.ITEM);
        if (source != null) {
            source.setSettings(definition.source());
        }
        return stack;
    }

    public static Set<ResourceLocation> ids() {
        return definitions.keySet();
    }

    public static void clear() {
        definitions = Map.of();
    }
}
