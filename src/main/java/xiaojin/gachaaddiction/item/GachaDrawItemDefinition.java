package xiaojin.gachaaddiction.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.api.GachaSourceSettings;

import java.util.List;
import java.util.Optional;

public record GachaDrawItemDefinition(ResourceLocation cardPool, ResourceLocation drawPackage,
                                      GachaSourceSettings source, Optional<ItemStack> item,
                                      List<ResourceLocation> previewItems) {
    public static final Codec<GachaDrawItemDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("card_pool").forGetter(GachaDrawItemDefinition::cardPool),
            ResourceLocation.CODEC.fieldOf("package").forGetter(GachaDrawItemDefinition::drawPackage),
            GachaSourceSettings.CODEC.optionalFieldOf("source", GachaSourceSettings.DEFAULT).forGetter(GachaDrawItemDefinition::source),
            ItemStack.CODEC.optionalFieldOf("item").forGetter(GachaDrawItemDefinition::item),
            ResourceLocation.CODEC.listOf().optionalFieldOf("preview_items", List.of()).forGetter(GachaDrawItemDefinition::previewItems)
    ).apply(instance, GachaDrawItemDefinition::new));
}
