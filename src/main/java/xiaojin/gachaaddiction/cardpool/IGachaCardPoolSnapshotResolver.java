package xiaojin.gachaaddiction.cardpool;

import java.util.Optional;

@FunctionalInterface
public interface IGachaCardPoolSnapshotResolver {
    Optional<GachaCardPoolDefinition> resolveSnapshot(String contentHash);
}
