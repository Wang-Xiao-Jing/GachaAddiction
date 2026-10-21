package xiaojin.gachaaddiction.persistence;

import net.minecraft.nbt.CompoundTag;

import java.io.IOException;
import java.util.Optional;

public interface GachaPoolSnapshotArchive {
    String store(CompoundTag snapshot) throws IOException;

    Optional<CompoundTag> load(String contentHash) throws IOException;
}
