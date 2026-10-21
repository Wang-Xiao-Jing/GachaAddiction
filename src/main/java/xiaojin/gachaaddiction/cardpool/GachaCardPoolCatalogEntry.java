package xiaojin.gachaaddiction.cardpool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record GachaCardPoolCatalogEntry(ResourceLocation id, String titleTranslationKey,
                                        Optional<String> descriptionTranslationKey,
                                        ItemStack icon,
                                        List<GachaDrawPackageSummary> packages,
                                        List<GachaPlayerChoiceSummary> playerChoices) {
    public GachaCardPoolCatalogEntry {
        Objects.requireNonNull(id, "id");
        if (Objects.requireNonNull(titleTranslationKey, "titleTranslationKey").isBlank()) {
            throw new IllegalArgumentException("Card pool catalog title cannot be blank");
        }
        descriptionTranslationKey = Objects.requireNonNull(descriptionTranslationKey,
                "descriptionTranslationKey");
        icon = Objects.requireNonNull(icon, "icon").copy();
        packages = List.copyOf(packages);
        playerChoices = List.copyOf(playerChoices);
        if (packages.isEmpty()) {
            throw new IllegalArgumentException("Card pool catalog entry must contain a draw package");
        }
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }
}
