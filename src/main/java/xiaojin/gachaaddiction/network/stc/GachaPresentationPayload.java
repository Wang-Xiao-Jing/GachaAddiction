package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.client.GachaPresentationClient;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.Optional;
import java.util.UUID;

public record GachaPresentationPayload(
        UUID token,
        int menuId,
        boolean forceWatch,
        boolean ended,
        ItemStack trigger,
        Optional<GachaType> animation,
        GachaSecondaryType secondaryType,
        int objectCount
) implements IToClientPayload {
    public static final Type<GachaPresentationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "presentation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaPresentationPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GachaPresentationPayload decode(RegistryFriendlyByteBuf buffer) {
            return new GachaPresentationPayload(buffer.readUUID(), buffer.readVarInt(), buffer.readBoolean(),
                    buffer.readBoolean(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    buffer.readBoolean() ? Optional.of(GachaType.STREAM_CODEC.decode(buffer)) : Optional.empty(),
                    GachaSecondaryType.STREAM_CODEC.decode(buffer), Math.clamp(buffer.readVarInt(), 1, GachaSecondaryType.MAXIMUM_ROUNDS));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, GachaPresentationPayload value) {
            buffer.writeUUID(value.token());
            buffer.writeVarInt(value.menuId());
            buffer.writeBoolean(value.forceWatch());
            buffer.writeBoolean(value.ended());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, value.trigger());
            buffer.writeBoolean(value.animation().isPresent());
            value.animation().ifPresent(type -> GachaType.STREAM_CODEC.encode(buffer, type));
            GachaSecondaryType.STREAM_CODEC.encode(buffer, value.secondaryType());
            buffer.writeVarInt(value.objectCount());
        }
    };
    public GachaPresentationPayload(UUID token, int menuId, boolean forceWatch, boolean ended,
                                    ItemStack trigger, Optional<GachaType> animation) {
        this(token, menuId, forceWatch, ended, trigger, animation, GachaSecondaryType.NONE, 1);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        GachaPresentationClient.receive(this);
    }
}
