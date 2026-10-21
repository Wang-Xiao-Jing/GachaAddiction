package xiaojin.gachaaddiction.data;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public record GachaRawDefinitionSnapshot(
        Map<GachaDefinitionKind, Map<ResourceLocation, GachaRawDefinition>> definitions) {
    public static final GachaRawDefinitionSnapshot EMPTY = new GachaRawDefinitionSnapshot(Map.of());

    public GachaRawDefinitionSnapshot(Map<GachaDefinitionKind, Map<ResourceLocation, GachaRawDefinition>> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        Map<GachaDefinitionKind, Map<ResourceLocation, GachaRawDefinition>> copied =
                new EnumMap<>(GachaDefinitionKind.class);
        definitions.forEach((kind, entries) -> copied.put(kind,
                Collections.unmodifiableMap(new LinkedHashMap<>(entries))));
        this.definitions = Collections.unmodifiableMap(copied);
    }

    public Map<ResourceLocation, GachaRawDefinition> definitions(GachaDefinitionKind kind) {
        return definitions.getOrDefault(Objects.requireNonNull(kind, "kind"), Map.of());
    }

    public Optional<GachaRawDefinition> find(GachaDefinitionKind kind, ResourceLocation id) {
        return Optional.ofNullable(definitions(kind).get(Objects.requireNonNull(id, "id")));
    }
}
