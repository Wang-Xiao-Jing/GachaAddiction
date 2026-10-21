package xiaojin.gachaaddiction.persistence;

import net.minecraft.nbt.CompoundTag;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinition;

public interface GachaCardPoolSnapshotCodec {
    CompoundTag encode(GachaCardPoolDefinition cardPool);

    GachaCardPoolDefinition decode(CompoundTag snapshot);
}
