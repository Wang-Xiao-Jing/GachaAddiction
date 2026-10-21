package xiaojin.gachaaddiction.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;
import xiaojin.gachaaddiction.cardpool.transaction.GachaTransactionId;

public final class GachaRewardInboxCodecs {
    public static final int MAXIMUM_REWARD_STACKS_PER_ENTRY = 4096;
    public static final int MAXIMUM_INBOX_ENTRIES = 4096;
    private static final StreamCodec<RegistryFriendlyByteBuf, GachaRewardInboxEntryKind> ENTRY_KIND = StreamCodec.of(
            FriendlyByteBuf::writeEnum, buffer -> buffer.readEnum(GachaRewardInboxEntryKind.class));

    public static final StreamCodec<RegistryFriendlyByteBuf, GachaRewardInboxEntry> ENTRY = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, entry -> entry.transactionId().value(),
            UUIDUtil.STREAM_CODEC, GachaRewardInboxEntry::playerId,
            ENTRY_KIND, GachaRewardInboxEntry::kind,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAXIMUM_REWARD_STACKS_PER_ENTRY)),
            GachaRewardInboxEntry::rewards,
            (transactionId, playerId, kind, rewards) -> new GachaRewardInboxEntry(
                    new GachaTransactionId(transactionId), playerId, kind, rewards));

    private GachaRewardInboxCodecs() {
    }
}
