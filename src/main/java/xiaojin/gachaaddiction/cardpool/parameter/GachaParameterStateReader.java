package xiaojin.gachaaddiction.cardpool.parameter;

import java.util.Optional;

@FunctionalInterface
public interface GachaParameterStateReader {
    <T> Optional<T> read(GachaParameterBinding<T> binding, GachaParameterBindingContext context);
}
