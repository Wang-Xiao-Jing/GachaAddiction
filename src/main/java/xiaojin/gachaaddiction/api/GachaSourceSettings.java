package xiaojin.gachaaddiction.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record GachaSourceSettings(Optional<ResourceLocation> gachaType, boolean forceWatch,
                                  Optional<GachaSecondaryType> secondaryType, int choiceCount) {
    public static final GachaSourceSettings DEFAULT = new GachaSourceSettings(Optional.empty(), false);
    public static final String TYPE_KEY = "gacha_type";
    public static final String FORCE_WATCH_KEY = "force_watch";
    public static final String SECONDARY_TYPE_KEY = "secondary_type";
    public static final String CHOICE_COUNT_KEY = "choice_count";
    public static final Codec<GachaSourceSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf(TYPE_KEY).forGetter(GachaSourceSettings::gachaType),
            Codec.BOOL.optionalFieldOf(FORCE_WATCH_KEY, false).forGetter(GachaSourceSettings::forceWatch),
            GachaSecondaryType.CODEC.optionalFieldOf(SECONDARY_TYPE_KEY).forGetter(GachaSourceSettings::secondaryType),
            Codec.intRange(2, GachaSecondaryType.MAXIMUM_CHOICES).optionalFieldOf(CHOICE_COUNT_KEY,
                    GachaSecondaryType.DEFAULT_CHOICES).forGetter(GachaSourceSettings::choiceCount)
    ).apply(instance, GachaSourceSettings::new));
    public GachaSourceSettings(Optional<ResourceLocation> gachaType, boolean forceWatch) {
        this(gachaType, forceWatch, Optional.empty(), GachaSecondaryType.DEFAULT_CHOICES);
    }
}
