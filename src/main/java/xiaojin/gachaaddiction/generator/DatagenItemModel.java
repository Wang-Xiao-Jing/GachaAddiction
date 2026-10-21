package xiaojin.gachaaddiction.generator;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;

public class DatagenItemModel extends ItemModelProvider {
    public DatagenItemModel(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(GachaDrawItems.SEALED_CHEST.get());
        basicItem(GachaDrawItems.GACHA_KEY.get());
        basicItem(GachaDrawItems.WHEEL_TICKET.get());
        basicItem(GachaDrawItems.SLOT_TOKEN.get());
        basicItem(GachaDrawItems.WISH_TOTEM.get());
    }
}