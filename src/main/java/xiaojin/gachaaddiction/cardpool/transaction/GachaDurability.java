package xiaojin.gachaaddiction.cardpool.transaction;

public enum GachaDurability {
    SYNC,
    GROUP_COMMIT,
    ASYNC;

    public static GachaDurability strongest(GachaDurability first, GachaDurability second) {
        return strength(first) >= strength(second) ? first : second;
    }

    private static int strength(GachaDurability durability) {
        return switch (durability) {
            case ASYNC -> 0;
            case GROUP_COMMIT -> 1;
            case SYNC -> 2;
        };
    }
}
