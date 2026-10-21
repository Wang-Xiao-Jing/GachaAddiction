package xiaojin.gachaaddiction.reward.problem;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record GachaProblem(ResourceLocation code, GachaProblemSeverity severity, String translationKey,
                           Map<String, String> context) {
    public GachaProblem {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(translationKey, "translationKey");
        context = Map.copyOf(new LinkedHashMap<>(context));
    }
}
