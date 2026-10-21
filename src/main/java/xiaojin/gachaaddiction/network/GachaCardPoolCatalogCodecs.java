package xiaojin.gachaaddiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolCatalogEntry;
import xiaojin.gachaaddiction.cardpool.GachaDrawPackageSummary;
import xiaojin.gachaaddiction.cardpool.GachaPlayerChoiceKind;
import xiaojin.gachaaddiction.cardpool.GachaPlayerChoiceSummary;
import xiaojin.gachaaddiction.cardpool.parameter.GachaPlayerChoiceLimits;

import java.util.List;
import java.util.Optional;

public final class GachaCardPoolCatalogCodecs {
    public static final int MAXIMUM_CARD_POOLS = 4096;
    public static final int MAXIMUM_PACKAGES_PER_POOL = 4096;

    public static final StreamCodec<RegistryFriendlyByteBuf, GachaPlayerChoiceSummary> PLAYER_CHOICE = new StreamCodec<>() {
        @Override
        public GachaPlayerChoiceSummary decode(RegistryFriendlyByteBuf buffer) {
            ResourceLocation owner = ResourceLocation.STREAM_CODEC.decode(buffer);
            String name = ByteBufCodecs.STRING_UTF8.decode(buffer);
            ResourceLocation parameterType = ResourceLocation.STREAM_CODEC.decode(buffer);
            String label = ByteBufCodecs.STRING_UTF8.decode(buffer);
            GachaPlayerChoiceKind kind = buffer.readEnum(GachaPlayerChoiceKind.class);
            Optional<String> minimum = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buffer);
            Optional<String> maximum = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buffer);
            Optional<String> step = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buffer);
            List<String> values = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(
                    GachaPlayerChoiceLimits.MAXIMUM_ENUMERATED_VALUES)).decode(buffer);
            return new GachaPlayerChoiceSummary(owner, name, parameterType, label, kind, minimum, maximum,
                    step, values);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, GachaPlayerChoiceSummary value) {
            ResourceLocation.STREAM_CODEC.encode(buffer, value.owner());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.name());
            ResourceLocation.STREAM_CODEC.encode(buffer, value.parameterType());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.labelTranslationKey());
            buffer.writeEnum(value.kind());
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buffer, value.minimum());
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buffer, value.maximum());
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buffer, value.step());
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(
                            GachaPlayerChoiceLimits.MAXIMUM_ENUMERATED_VALUES))
                    .encode(buffer, value.values());
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, GachaDrawPackageSummary> PACKAGE =
            StreamCodec.composite(ResourceLocation.STREAM_CODEC, GachaDrawPackageSummary::id,
                    GachaDrawPackageSummary::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, GachaCardPoolCatalogEntry> CARD_POOL = new StreamCodec<>() {
        @Override
        public GachaCardPoolCatalogEntry decode(RegistryFriendlyByteBuf buffer) {
            return new GachaCardPoolCatalogEntry(ResourceLocation.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).decode(buffer),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    PACKAGE.apply(ByteBufCodecs.list(MAXIMUM_PACKAGES_PER_POOL)).decode(buffer),
                    PLAYER_CHOICE.apply(ByteBufCodecs.list(
                            GachaPlayerChoiceLimits.MAXIMUM_CHOICES_PER_CARD_POOL)).decode(buffer));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, GachaCardPoolCatalogEntry value) {
            ResourceLocation.STREAM_CODEC.encode(buffer, value.id());
            ByteBufCodecs.STRING_UTF8.encode(buffer, value.titleTranslationKey());
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8).encode(buffer,
                    value.descriptionTranslationKey());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, value.icon());
            PACKAGE.apply(ByteBufCodecs.list(MAXIMUM_PACKAGES_PER_POOL)).encode(buffer, value.packages());
            PLAYER_CHOICE.apply(ByteBufCodecs.list(GachaPlayerChoiceLimits.MAXIMUM_CHOICES_PER_CARD_POOL))
                    .encode(buffer, value.playerChoices());
        }
    };

    private GachaCardPoolCatalogCodecs() {
    }
}
