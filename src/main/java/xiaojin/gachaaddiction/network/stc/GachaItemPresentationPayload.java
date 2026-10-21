package xiaojin.gachaaddiction.network.stc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.List;
import java.util.UUID;

public record GachaItemPresentationPayload(
        UUID session,
        GachaType animation,
        boolean completed,
        boolean success,
        long lifetimeMillis,
        List<ItemStack> items,
        List<List<ItemStack>> rewardRounds
) implements IToClientPayload {
    public static final Type<GachaItemPresentationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "item_draw_presentation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaItemPresentationPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public GachaItemPresentationPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new GachaItemPresentationPayload(buffer.readUUID(), GachaType.STREAM_CODEC.decode(buffer),
                            buffer.readBoolean(), buffer.readBoolean(), buffer.readVarLong(), ITEMS.decode(buffer), ROUNDS.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, GachaItemPresentationPayload value) {
                    buffer.writeUUID(value.session);
                    GachaType.STREAM_CODEC.encode(buffer, value.animation);
                    buffer.writeBoolean(value.completed);
                    buffer.writeBoolean(value.success);
                    buffer.writeVarLong(value.lifetimeMillis);
                    ITEMS.encode(buffer, value.items);
                    ROUNDS.encode(buffer, value.rewardRounds);
                }
            };
    private static final int MAXIMUM_STACKS = 4096;
    private static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> ITEMS =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAXIMUM_STACKS));
    private static final StreamCodec<RegistryFriendlyByteBuf, List<List<ItemStack>>> ROUNDS = ITEMS.apply(ByteBufCodecs.list(MAXIMUM_STACKS));

    public GachaItemPresentationPayload(UUID session, GachaType animation, boolean completed,
                                        boolean success, long lifetimeMillis, List<ItemStack> items) {
        this(session, animation, completed, success, lifetimeMillis, items, List.of());
    }

    public GachaItemPresentationPayload {
        items = items.stream().map(ItemStack::copy).toList();
        rewardRounds = rewardRounds.stream().map(round -> round.stream().map(ItemStack::copy).toList()).toList();
        if (rewardRounds.size() > MAXIMUM_STACKS
                || rewardRounds.stream().mapToLong(List::size).sum() > MAXIMUM_STACKS) {
            throw new IllegalArgumentException("Too many grouped reward stacks");
        }
    }

    @Override
    public List<ItemStack> items() {
        return items.stream().map(ItemStack::copy).toList();
    }

    @Override
    public List<List<ItemStack>> rewardRounds() {
        return rewardRounds.stream().map(round -> round.stream().map(ItemStack::copy).toList()).toList();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
