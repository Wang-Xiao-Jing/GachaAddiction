package xiaojin.gachaaddiction.persistence;

import xiaojin.gachaaddiction.cardpool.GachaCardPoolDefinition;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolSnapshotManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GachaCardPoolSnapshotStore implements GachaCardPoolSnapshotManager {
    private final GachaPoolSnapshotArchive archive;
    private final GachaCardPoolSnapshotCodec codec;
    private final Map<String, GachaCardPoolDefinition> cache = new ConcurrentHashMap<>();

    public GachaCardPoolSnapshotStore(GachaPoolSnapshotArchive archive, GachaCardPoolSnapshotCodec codec) {
        this.archive = Objects.requireNonNull(archive, "archive");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public String store(GachaCardPoolDefinition cardPool) {
        Objects.requireNonNull(cardPool, "cardPool");
        try {
            String contentHash = archive.store(codec.encode(cardPool));
            cache.putIfAbsent(contentHash, cardPool);
            return contentHash;
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to archive card pool snapshot", failure);
        }
    }

    @Override
    public Optional<GachaCardPoolDefinition> resolveSnapshot(String contentHash) {
        GachaCardPoolDefinition cached = cache.get(contentHash);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            return archive.load(contentHash).map(snapshot -> {
                GachaCardPoolDefinition decoded = codec.decode(snapshot);
                cache.putIfAbsent(contentHash, decoded);
                return decoded;
            });
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to load card pool snapshot", failure);
        }
    }
}
