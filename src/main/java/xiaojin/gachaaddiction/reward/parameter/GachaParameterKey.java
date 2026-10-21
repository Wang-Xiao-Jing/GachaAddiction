package xiaojin.gachaaddiction.reward.parameter;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record GachaParameterKey(ResourceLocation owner, String name) implements Comparable<GachaParameterKey> {
    private static final String NAME_PATTERN = "[a-z0-9_.-]+";

    public GachaParameterKey {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
        if (!name.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException("Invalid parameter name: " + name);
        }
    }

    @Override
    public int compareTo(GachaParameterKey other) {
        int ownerComparison = owner.compareTo(other.owner);
        return ownerComparison != 0 ? ownerComparison : name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return owner + "#" + name;
    }
}
