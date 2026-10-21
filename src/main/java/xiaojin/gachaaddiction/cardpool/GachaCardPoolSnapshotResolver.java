package xiaojin.gachaaddiction.cardpool;

import java.util.Optional;

@FunctionalInterface
public interface GachaCardPoolSnapshotResolver {
    Optional<GachaCardPoolDefinition> resolveSnapshot(String contentHash);
}
