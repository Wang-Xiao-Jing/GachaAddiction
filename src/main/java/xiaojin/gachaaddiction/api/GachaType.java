package xiaojin.gachaaddiction.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import xiaojin.gachaaddiction.api.client.GachaTypeClient;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.init.client.GachaTypeClients;
import xiaojin.gachaaddiction.util.ModUtil;

import java.util.Optional;

public class GachaType {
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaType> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, GachaType::getId,
            GachaTypes::getDefault);

    private final Identifier id;
    private final String translationKey ;

    public GachaType(Identifier id) {
        this.id = id;
        translationKey = "gacha_type." + id;
    }

    public Identifier getId() {
        return id;
    }

    public GachaTypeClient<?, ?> getClientData() {
        return GachaTypeClients.get(this);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public boolean isEmpty() {
        return GachaTypes.EMPTY == this;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
