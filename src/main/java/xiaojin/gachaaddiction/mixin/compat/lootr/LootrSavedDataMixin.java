package xiaojin.gachaaddiction.mixin.compat.lootr;

import noobanidus.mods.lootr.common.api.data.ILootrInfo;
import noobanidus.mods.lootr.common.data.LootrInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiaojin.gachaaddiction.mixed.IILootrInventory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Pseudo
@Mixin(targets = "noobanidus.mods.lootr.common.data.LootrSavedData")
public abstract class LootrSavedDataMixin {
    @Inject(method = "<init>(ZLnoobanidus/mods/lootr/common/api/data/ILootrInfo;Ljava/util/Map;Ljava/util/Set;Ljava/util/Set;)V",
            at = @At("RETURN"))
    private void gachaaddiction$load(boolean hasBeenOpened, ILootrInfo info, Map<UUID, LootrInventory> inventories,
                                     Set<UUID> openers, Set<UUID> actualOpeners, CallbackInfo ci) {
        for (LootrInventory inventory : inventories.values()) {
            IILootrInventory.of(inventory).gachaaddiction$setOpen(true);
        }
    }
}
