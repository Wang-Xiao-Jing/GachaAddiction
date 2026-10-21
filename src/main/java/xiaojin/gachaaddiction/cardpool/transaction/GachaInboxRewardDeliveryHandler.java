package xiaojin.gachaaddiction.cardpool.transaction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class GachaInboxRewardDeliveryHandler implements GachaRewardDeliveryHandler {
    private static final Set<GachaRewardDeliveryCapability> CAPABILITIES = Set.of(
            GachaRewardDeliveryCapability.IDEMPOTENT_BY_TRANSACTION,
            GachaRewardDeliveryCapability.DURABLE_BEFORE_ACKNOWLEDGEMENT,
            GachaRewardDeliveryCapability.SUPPORTS_OFFLINE_PLAYER);

    private final ResourceLocation type;
    private final GachaRewardInboxEntryKind kind;

    public GachaInboxRewardDeliveryHandler(ResourceLocation type, GachaRewardInboxEntryKind kind) {
        this.type = Objects.requireNonNull(type, "type");
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    @Override
    public ResourceLocation type() {
        return type;
    }

    @Override
    public Set<GachaRewardDeliveryCapability> capabilities() {
        return CAPABILITIES;
    }

    @Override
    public void deliver(GachaTransactionContext context, List<ItemStack> rewards,
                        GachaRewardDeliveryServices services) {
        services.rewardInbox().deliver(context, rewards, kind);
    }
}
