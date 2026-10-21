package xiaojin.gachaaddiction.reward.custom;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

@FunctionalInterface
public interface IGachaItemTagResolver {
    List<Item> resolve(TagKey<Item> tag);
}
