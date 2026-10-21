package xiaojin.gachaaddiction.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.generator.KubeResourceGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.item.GachaDrawItemDefinitions;

public class GachaDrawItemKubeJS {
    public static void define(KubeResourceGenerator event, String definitionId, JsonObject definition) {
        ResourceLocation id = ResourceLocation.parse(definitionId);
        event.json(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                GachaDrawItemDefinitions.DIRECTORY + "/" + id.getPath() + ".json"), definition);
    }

    public static ItemStack create(String definitionId) {
        return GachaDrawItemDefinitions.create(definitionId);
    }
}
