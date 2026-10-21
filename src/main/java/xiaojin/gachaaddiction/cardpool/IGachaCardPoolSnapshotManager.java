package xiaojin.gachaaddiction.cardpool;

public interface IGachaCardPoolSnapshotManager extends IGachaCardPoolSnapshotResolver {
    String store(GachaCardPoolDefinition cardPool);
}
