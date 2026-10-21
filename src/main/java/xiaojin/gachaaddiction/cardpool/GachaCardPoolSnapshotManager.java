package xiaojin.gachaaddiction.cardpool;

public interface GachaCardPoolSnapshotManager extends GachaCardPoolSnapshotResolver {
    String store(GachaCardPoolDefinition cardPool);
}
