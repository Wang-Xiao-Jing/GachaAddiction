package xiaojin.gachaaddiction.api;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.api.client.GachaTypeClient;
import xiaojin.gachaaddiction.init.client.GachaTypeClients;

public class GachaType {
    private final ResourceLocation id;

    public GachaType(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public GachaTypeClient<?, ?> getClientData() {
        return GachaTypeClients.get(this);
    }
}
