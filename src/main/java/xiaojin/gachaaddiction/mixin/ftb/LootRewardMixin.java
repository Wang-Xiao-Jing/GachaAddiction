package xiaojin.gachaaddiction.mixin.ftb;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.client.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.LootReward;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.util.FtbUtil;
import xiaojin.gachaaddiction.mixed.ILootRewardModeMap;

import java.util.ArrayList;
import java.util.List;

@Mixin(LootReward.class)
public abstract class LootRewardMixin extends RandomReward implements ILootRewardModeMap {
    @Unique
    private GachaType gachaaddiction$gachaType = GachaTypes.EMPTY;

    public LootRewardMixin(long id, Quest parent) {
        super(id, parent);
    }

    @WrapOperation(method = "onButtonClicked", remap = false, at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/client/gui/RewardNotificationsScreen;openGui()V"))
    private void gachaaddiction$onButtonClicked(RewardNotificationsScreen instance, Operation<Void> original) {
        original.call(instance);
        if (!GachaAddictionConfig.CLIENT.ftbQuestsGachaa.get()) {
            return;
        }

        GachaType gachaType = gachaaddiction$getGachaType();
        if (gachaType.isEmpty()) {
            gachaType = GachaAddictionConfig.CLIENT.getDefaultGachaaType();
            if (gachaType.isEmpty()) {
                return;
            }
        }

        RewardTable table = getTable();
        List<ItemStackEntry> itemStackEntries;
        if (table == null) {
            itemStackEntries = List.of();
        } else {
            itemStackEntries = new ArrayList<>();
            for (WeightedReward weightedReward : table.getWeightedRewards()) {
                Reward reward = weightedReward.getReward();
                if (reward instanceof ItemReward itemReward) {
                    ItemStack itemStack = itemReward.getItem().copyWithCount(itemReward.getCount());
                    int weight = (int) (weightedReward.getWeight() * 100);
                    itemStackEntries.add(new ItemStackEntry(itemStack, weight));
                }
            }
        }

        gachaType.getClientData()
                .open(List.of(), itemStackEntries)
                .setReturnOriginalScreen(false);
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        if (this.getTable() != null) {
            nbt.putString("gachaaddiction:gacha_type", gachaaddiction$getGachaTypeIdString());
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        if (this.getTable() != null) {
            gachaaddiction$gachaType = FtbUtil.byId(nbt.getString("gachaaddiction:gacha_type"));
        }
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        FtbUtil.NAME_MAP.write(buffer, gachaaddiction$gachaType);
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addEnum(GachaAddiction.MODID + ":gacha_type", gachaaddiction$gachaType,
                (v) -> gachaaddiction$gachaType = v,
                FtbUtil.NAME_MAP, GachaTypes.EMPTY);
    }

    @Override
    public GachaType gachaaddiction$getGachaType() {
        return gachaaddiction$gachaType;
    }

    @Override
    public void gachaaddiction$setGachaType(GachaType gachaaddiction$gachaType) {
        this.gachaaddiction$gachaType = gachaaddiction$gachaType;
    }

    @Override
    public String gachaaddiction$getGachaTypeIdString() {
        return gachaaddiction$getGachaTypeId().toString();
    }

    @Override
    public ResourceLocation gachaaddiction$getGachaTypeId() {
        return gachaaddiction$gachaType.getId();
    }
}
