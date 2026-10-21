package xiaojin.gachaaddiction.cardpool.parameter;

import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDirection;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GachaCardPoolParameterResolver {
    private static <T> Optional<T> resolveBinding(GachaParameterBinding<T> binding,
                                                  GachaParameterSet packageParameters,
                                                  GachaParameterSet requestParameters,
                                                  GachaRequestAuthority authority,
                                                  GachaParameterBindingContext context,
                                                  GachaParameterStateReader stateReader) {
        GachaParameterDefinition<T> definition = binding.parameter();
        Optional<T> baseValue = baseValue(binding, requestParameters, context, stateReader);
        Optional<T> packageValue = packageParameters.get(definition);
        Optional<T> requestValue = requestParameters.get(definition);
        Optional<T> overriddenValue = switch (binding.overridePolicy()) {
            case LOCKED -> baseValue;
            case PACKAGE_VALUE -> packageValue.or(() -> baseValue);
            case PLAYER_CHOICE -> requestValue.map(binding.playerChoice().orElseThrow()::requireAllowed)
                    .or(() -> packageValue).or(() -> baseValue);
            case TRUSTED_REQUEST -> authority == GachaRequestAuthority.PLAYER
                    ? packageValue.or(() -> baseValue) : requestValue.or(() -> packageValue).or(() -> baseValue);
            case ADMIN_OVERRIDE -> authority == GachaRequestAuthority.ADMIN
                    ? requestValue.or(() -> packageValue).or(() -> baseValue) : packageValue.or(() -> baseValue);
        };
        Optional<T> value = overriddenValue.or(definition::defaultValue);
        if (definition.direction() != GachaParameterDirection.OUTPUT && definition.required() && value.isEmpty()) {
            throw new IllegalArgumentException("Missing required card pool parameter: " + definition.key());
        }
        return value;
    }

    private static <T> Optional<T> baseValue(GachaParameterBinding<T> binding,
                                             GachaParameterSet requestParameters,
                                             GachaParameterBindingContext context,
                                             GachaParameterStateReader stateReader) {
        return switch (binding.source()) {
            case CONSTANT -> binding.boundValue();
            case REQUEST -> requestParameters.get(binding.parameter());
            case PLAYER_BANNER, PLAYER_SHARED, SERVER_BANNER, SERVER_SHARED -> stateReader.read(binding, context);
        };
    }

    public GachaParameterResolution resolve(List<GachaParameterBinding<?>> bindings,
                                            GachaParameterSchema schema,
                                            GachaParameterSet packageParameters,
                                            GachaParameterSet requestParameters, GachaRequestAuthority authority,
                                            GachaParameterBindingContext context,
                                            GachaParameterStateReader stateReader,
                                            GachaParameterFingerprint fingerprint) {
        Objects.requireNonNull(bindings, "bindings");
        Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(packageParameters, "packageParameters");
        Objects.requireNonNull(requestParameters, "requestParameters");
        Objects.requireNonNull(authority, "authority");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(stateReader, "stateReader");
        Objects.requireNonNull(fingerprint, "fingerprint");
        GachaParameterSet.Builder resolved = GachaParameterSet.builder();
        for (GachaParameterBinding<?> binding : bindings) {
            resolveBinding(binding, packageParameters, requestParameters, authority, context, stateReader)
                    .ifPresent(value -> resolved.put(binding.parameter(), value));
        }
        GachaParameterSet parameters = schema.normalizeInputs(resolved.build());
        return new GachaParameterResolution(parameters, fingerprint.create(schema, parameters));
    }
}
