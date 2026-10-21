package xiaojin.gachaaddiction.execution;

import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface IGachaParameterMappingDecoder {
    GachaParameterMapping decode(ResourceLocation sourceOwner, ResourceLocation targetOwner,
                                 JsonArray mappings, String location);
}
