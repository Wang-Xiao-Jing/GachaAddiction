package xiaojin.gachaaddiction.mixin.compat.ftb;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.LootReward;
import dev.ftb.mods.ftbquests.quest.reward.RandomReward;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.mixed.ILootRewardModeMap;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.util.FtbUtil;


@Mixin(LootReward.class)
public abstract class LootRewardMixin extends RandomReward implements ILootRewardModeMap {
    @Unique
    private GachaType gachaaddiction$gachaType = GachaTypes.EMPTY;

    public LootRewardMixin(long id, Quest parent) {
        super(id, parent);
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
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        gachaaddiction$gachaType = FtbUtil.NAME_MAP.read(buffer);
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addEnum(GachaConstants.MODID + ":gacha_type", gachaaddiction$gachaType,
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
