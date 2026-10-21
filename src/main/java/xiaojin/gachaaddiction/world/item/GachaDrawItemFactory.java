package xiaojin.gachaaddiction.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.data.drawitem.GachaDrawItemDefinition;
import xiaojin.gachaaddiction.data.drawitem.GachaDrawItemDefinitions;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;

import java.util.Optional;

public class GachaDrawItemFactory {
    public static final String DEFINITION_KEY = "draw_item";
    private static final String CARD_POOL_KEY = "card_pool";
    private static final String PACKAGE_KEY = "package";
    private static final String PREVIEW_KEY = "preview_items";

    public static Optional<GachaDrawItemDefinition> find(ItemStack stack) {
        ResourceLocation id = ResourceLocation.tryParse(binding(stack).getString(DEFINITION_KEY));
        return GachaDrawItemDefinitions.find(id);
    }

    public static CompoundTag binding(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(GachaConstants.MODID);
    }

    public static CompoundTag resolveBinding(ItemStack stack) {
        CompoundTag binding = binding(stack);
        if (!binding.contains(DEFINITION_KEY)) {
            return binding;
        }

        ResourceLocation id = ResourceLocation.tryParse(binding.getString(DEFINITION_KEY));
        GachaDrawItemDefinition definition = GachaDrawItemDefinitions.find(id).orElse(null);
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
        GachaDrawItemDefinition definition = GachaDrawItemDefinitions.find(id).orElse(null);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown draw item definition: " + id);
        }

        ItemStack stack = definition.item().map(ItemStack::copy).orElseGet(GachaDrawItems.WISH_TOTEM::toStack);
        stack.setCount(1);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag data = root.getCompound(GachaConstants.MODID);
            data.putString(DEFINITION_KEY, id.toString());
            root.put(GachaConstants.MODID, data);
        });
        var source = stack.getCapability(GachaSources.ITEM);
        if (source != null) {
            source.setSettings(definition.source());
        }
        return stack;
    }
}
