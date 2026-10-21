package xiaojin.gachaaddiction.reward.quantity;

import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

import java.util.Objects;

public record GachaQuantityContext(GachaNumberContext numbers) {
    public GachaQuantityContext {
        Objects.requireNonNull(numbers, "numbers");
    }
}
