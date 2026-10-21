package xiaojin.gachaaddiction.admin;

import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.data.GachaDefinitionKind;

import java.util.Objects;

public record GachaDefinitionDocumentKey(GachaDefinitionKind kind, ResourceLocation id)
        implements Comparable<GachaDefinitionDocumentKey> {
    public GachaDefinitionDocumentKey {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
    }

    @Override
    public int compareTo(GachaDefinitionDocumentKey other) {
        int kindComparison = kind.compareTo(other.kind);
        return kindComparison != 0 ? kindComparison : id.toString().compareTo(other.id.toString());
    }
}
