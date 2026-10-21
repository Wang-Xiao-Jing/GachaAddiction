package xiaojin.gachaaddiction.network.stc;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolResultStyle;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.List;
import java.util.UUID;

public record OpenCardPoolResultPayload(
        UUID transactionId, ResourceLocation cardPoolId,
        ResourceLocation packageId, GachaCardPoolResultStyle resultStyle,
        List<ItemStack> rewards,
        List<List<ItemStack>> rewardRounds
) implements IToClientPayload {
    public static final Type<OpenCardPoolResultPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "open_card_pool_result"));
    private static final int MAXIMUM_REWARD_STACKS = 4096;
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCardPoolResultPayload> STREAM_CODEC =
            StreamCodec.composite(UUIDUtil.STREAM_CODEC, OpenCardPoolResultPayload::transactionId,
                    ResourceLocation.STREAM_CODEC, OpenCardPoolResultPayload::cardPoolId,
                    ResourceLocation.STREAM_CODEC, OpenCardPoolResultPayload::packageId,
                    new StreamCodec<>() {
                        @Override
                        public GachaCardPoolResultStyle decode(RegistryFriendlyByteBuf buffer) {
                            return buffer.readEnum(GachaCardPoolResultStyle.class);
                        }

                        @Override
                        public void encode(RegistryFriendlyByteBuf buffer, GachaCardPoolResultStyle value) {
                            buffer.writeEnum(value);
                        }
                    }, OpenCardPoolResultPayload::resultStyle,
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAXIMUM_REWARD_STACKS)),
                    OpenCardPoolResultPayload::rewards,
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAXIMUM_REWARD_STACKS)).apply(ByteBufCodecs.list(MAXIMUM_REWARD_STACKS)),
                    OpenCardPoolResultPayload::rewardRounds,
                    OpenCardPoolResultPayload::new);

    public OpenCardPoolResultPayload(UUID transactionId, ResourceLocation cardPoolId, ResourceLocation packageId,
                                     GachaCardPoolResultStyle resultStyle, List<ItemStack> rewards) {
        this(transactionId, cardPoolId, packageId, resultStyle, rewards, List.of());
    }

    public OpenCardPoolResultPayload {
        rewards = rewards.stream().map(ItemStack::copy).toList();
        rewardRounds = rewardRounds.stream().map(round -> round.stream().map(ItemStack::copy).toList()).toList();
        if (rewardRounds.size() > MAXIMUM_REWARD_STACKS
                || rewardRounds.stream().mapToLong(List::size).sum() > MAXIMUM_REWARD_STACKS) {
            throw new IllegalArgumentException("Too many grouped reward stacks");
        }
    }

    @Override
    public List<ItemStack> rewards() {
        return rewards.stream().map(ItemStack::copy).toList();
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
