package xiaojin.gachaaddiction.cardpool;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public record GachaCardPoolPresentation(String titleTranslationKey,
                                        Optional<String> descriptionTranslationKey,
                                        ItemStack icon, GachaCardPoolResultStyle resultStyle) {
    public GachaCardPoolPresentation {
        if (Objects.requireNonNull(titleTranslationKey, "titleTranslationKey").isBlank()) {
            throw new IllegalArgumentException("Card pool title translation key cannot be blank");
        }
        descriptionTranslationKey = Objects.requireNonNull(descriptionTranslationKey,
                "descriptionTranslationKey");
        descriptionTranslationKey.ifPresent(value -> {
            if (value.isBlank()) {
                throw new IllegalArgumentException("Card pool description translation key cannot be blank");
            }
        });
        icon = Objects.requireNonNull(icon, "icon").copy();
        if (icon.isEmpty()) {
            throw new IllegalArgumentException("Card pool presentation icon cannot be empty");
        }
        Objects.requireNonNull(resultStyle, "resultStyle");
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }
}
