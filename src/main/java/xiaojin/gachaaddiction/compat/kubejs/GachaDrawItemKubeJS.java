package xiaojin.gachaaddiction.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.generator.KubeResourceGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.data.drawitem.GachaDrawItemDefinitionReloadListener;
import xiaojin.gachaaddiction.world.item.GachaDrawItemFactory;

public class GachaDrawItemKubeJS {
    public static void define(KubeResourceGenerator event, String definitionId, JsonObject definition) {
        ResourceLocation id = ResourceLocation.parse(definitionId);
        event.json(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                GachaDrawItemDefinitionReloadListener.DIRECTORY + "/" + id.getPath() + ".json"), definition);
    }

    public static ItemStack create(String definitionId) {
        return GachaDrawItemFactory.create(definitionId);
    }
}
