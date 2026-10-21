package xiaojin.gachaaddiction.cardpool.parameter;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;

public final class GachaStateConflictException extends RuntimeException {
    public GachaStateConflictException(GachaParameterKey key) {
        super("Gacha parameter state changed during transaction: " + key);
    }
}
