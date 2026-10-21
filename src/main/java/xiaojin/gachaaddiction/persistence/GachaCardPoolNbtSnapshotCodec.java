package xiaojin.gachaaddiction.persistence;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.*;
import xiaojin.gachaaddiction.cardpool.parameter.*;
import xiaojin.gachaaddiction.cardpool.transaction.GachaDurability;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public final class GachaCardPoolNbtSnapshotCodec implements IGachaCardPoolSnapshotCodec {
    public static final int FORMAT_VERSION = 5;

    private static final String FORMAT_VERSION_KEY = "format_version";
    private static final String ID_KEY = "id";
    private static final String EXECUTION_PLAN_KEY = "execution_plan";
    private static final String DEFINITIONS_KEY = "parameter_definitions";
    private static final String BINDINGS_KEY = "parameter_bindings";
    private static final String PACKAGES_KEY = "packages";
    private static final String OWNER_KEY = "owner";
    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String DIRECTION_KEY = "direction";
    private static final String REQUIRED_KEY = "required";
    private static final String DEFAULT_KEY = "default";
    private static final String SOURCE_KEY = "source";
    private static final String OVERRIDE_POLICY_KEY = "override_policy";
    private static final String BOUND_VALUE_KEY = "bound_value";
    private static final String PLAYER_CHOICE_KEY = "player_choice";
    private static final String CHOICE_KIND_KEY = "kind";
    private static final String LABEL_KEY = "label";
    private static final String MINIMUM_KEY = "minimum";
    private static final String MAXIMUM_KEY = "maximum";
    private static final String STEP_KEY = "step";
    private static final String VALUES_KEY = "values";
    private static final String PARAMETERS_KEY = "parameters";
    private static final String MINIMUM_DURABILITY_KEY = "minimum_durability";
    private static final String PRESENTATION_KEY = "presentation";
    private static final String AVAILABILITY_KEY = "availability";
    private static final String DELIVERY_MODE_KEY = "delivery_mode";
    private static final String DELIVERY_HANDLER_KEY = "delivery_handler";
    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String ICON_KEY = "icon";
    private static final String RESULT_STYLE_KEY = "result_style";
    private static final String ENABLED_KEY = "enabled";
    private static final String MINIMUM_PERMISSION_LEVEL_KEY = "minimum_permission_level";
    private static final String OPENS_AT_KEY = "opens_at";
    private static final String CLOSES_AT_KEY = "closes_at";

    private final HolderLookup.Provider registries;
    private final IGachaParameterTypeResolver parameterTypes;
    private final DynamicOps<Tag> ops;

    public GachaCardPoolNbtSnapshotCodec(HolderLookup.Provider registries,
                                         IGachaParameterTypeResolver parameterTypes) {
        this.registries = Objects.requireNonNull(registries, "registries");
        this.parameterTypes = Objects.requireNonNull(parameterTypes, "parameterTypes");
        this.ops = RegistryOps.create(NbtOps.INSTANCE, registries);
    }

    private static CompoundTag encodeAvailability(GachaCardPoolAvailability availability) {
        CompoundTag encoded = new CompoundTag();
        encoded.putBoolean(ENABLED_KEY, availability.enabled());
        encoded.putInt(MINIMUM_PERMISSION_LEVEL_KEY, availability.minimumPermissionLevel());
        availability.opensAt().ifPresent(value -> encoded.putLong(OPENS_AT_KEY, value.toEpochMilli()));
        availability.closesAt().ifPresent(value -> encoded.putLong(CLOSES_AT_KEY, value.toEpochMilli()));
        return encoded;
    }

    private static GachaCardPoolAvailability decodeAvailability(CompoundTag encoded) {
        return new GachaCardPoolAvailability(encoded.getBoolean(ENABLED_KEY),
                encoded.getInt(MINIMUM_PERMISSION_LEVEL_KEY),
                encoded.contains(OPENS_AT_KEY) ? Optional.of(Instant.ofEpochMilli(encoded.getLong(OPENS_AT_KEY)))
                        : Optional.empty(),
                encoded.contains(CLOSES_AT_KEY) ? Optional.of(Instant.ofEpochMilli(encoded.getLong(CLOSES_AT_KEY)))
                        : Optional.empty());
    }

    @SuppressWarnings("unchecked")
    private static <T> GachaPlayerChoice<T> castPlayerChoice(GachaPlayerChoice<?> choice) {
        return (GachaPlayerChoice<T>) choice;
    }

    private static CompoundTag encodeKey(GachaParameterKey key) {
        CompoundTag encoded = new CompoundTag();
        encoded.putString(OWNER_KEY, key.owner().toString());
        encoded.putString(NAME_KEY, key.name());
        return encoded;
    }

    private static GachaParameterKey decodeKey(CompoundTag encoded) {
        return new GachaParameterKey(ResourceLocation.parse(encoded.getString(OWNER_KEY)),
                encoded.getString(NAME_KEY));
    }

    @Override
    public CompoundTag encode(GachaCardPoolDefinition cardPool) {
        CompoundTag encoded = new CompoundTag();
        encoded.putInt(FORMAT_VERSION_KEY, FORMAT_VERSION);
        encoded.putString(ID_KEY, cardPool.id().toString());
        GachaParameterSchema schema = cardPool.parameterSchema();
        encoded.put(DEFINITIONS_KEY, encodeDefinitions(schema));
        encoded.put(BINDINGS_KEY, encodeBindings(cardPool.parameterBindings()));
        encoded.put(PACKAGES_KEY, encodePackages(cardPool.packages(), schema));
        encoded.put(PRESENTATION_KEY, encodePresentation(cardPool.presentation()));
        encoded.put(AVAILABILITY_KEY, encodeAvailability(cardPool.availability()));
        encoded.putString(DELIVERY_MODE_KEY, cardPool.deliveryMode().name());
        cardPool.deliveryHandler().ifPresent(value -> encoded.putString(DELIVERY_HANDLER_KEY, value.toString()));
        return encoded;
    }

    @Override
    public GachaCardPoolDefinition decode(CompoundTag snapshot) {
        int version = snapshot.getInt(FORMAT_VERSION_KEY);
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported card pool snapshot version: " + version);
        }
        List<GachaParameterDefinition<?>> definitions = decodeDefinitions(snapshot);
        GachaParameterSchema schema = new GachaParameterSchema(definitions);
        List<GachaParameterBinding<?>> bindings = decodeBindings(snapshot, schema);
        ResourceLocation cardPoolId = ResourceLocation.parse(snapshot.getString(ID_KEY));
        Map<ResourceLocation, GachaDrawPackage> packages = decodePackages(snapshot, schema);
        GachaCardPoolPresentation presentation = decodePresentation(snapshot.getCompound(PRESENTATION_KEY));
        GachaCardPoolAvailability availability = decodeAvailability(snapshot.getCompound(AVAILABILITY_KEY));
        return new GachaCardPoolDefinition(cardPoolId, packages, bindings, schema,
                presentation, availability,
                GachaRewardDeliveryMode.valueOf(snapshot.getString(DELIVERY_MODE_KEY)),
                snapshot.contains(DELIVERY_HANDLER_KEY)
                        ? Optional.of(ResourceLocation.parse(snapshot.getString(DELIVERY_HANDLER_KEY)))
                        : Optional.empty());
    }

    private CompoundTag encodePresentation(GachaCardPoolPresentation presentation) {
        CompoundTag encoded = new CompoundTag();
        encoded.putString(TITLE_KEY, presentation.titleTranslationKey());
        presentation.descriptionTranslationKey().ifPresent(value -> encoded.putString(DESCRIPTION_KEY, value));
        encoded.put(ICON_KEY, presentation.icon().save(registries));
        encoded.putString(RESULT_STYLE_KEY, presentation.resultStyle().name());
        return encoded;
    }

    private GachaCardPoolPresentation decodePresentation(CompoundTag encoded) {
        return new GachaCardPoolPresentation(encoded.getString(TITLE_KEY),
                encoded.contains(DESCRIPTION_KEY) ? Optional.of(encoded.getString(DESCRIPTION_KEY))
                        : Optional.empty(),
                ItemStack.parseOptional(registries, encoded.getCompound(ICON_KEY)),
                GachaCardPoolResultStyle.valueOf(encoded.getString(RESULT_STYLE_KEY)));
    }

    private ListTag encodeDefinitions(GachaParameterSchema schema) {
        ListTag encoded = new ListTag();
        for (GachaParameterDefinition<?> definition : schema.definitions()) {
            CompoundTag entry = encodeKey(definition.key());
            entry.putString(TYPE_KEY, definition.type().id().toString());
            entry.putString(DIRECTION_KEY, definition.direction().name());
            entry.putBoolean(REQUIRED_KEY, definition.required());
            definition.defaultValue().ifPresent(value -> entry.put(DEFAULT_KEY,
                    encodeValue(definition.type(), value)));
            encoded.add(entry);
        }
        return encoded;
    }

    private ListTag encodeBindings(List<GachaParameterBinding<?>> bindings) {
        ListTag encoded = new ListTag();
        for (GachaParameterBinding<?> binding : bindings) {
            CompoundTag entry = encodeKey(binding.parameter().key());
            entry.putString(SOURCE_KEY, binding.source().name());
            entry.putString(OVERRIDE_POLICY_KEY, binding.overridePolicy().name());
            binding.boundValue().ifPresent(value -> entry.put(BOUND_VALUE_KEY,
                    encodeValue(binding.parameter().type(), value)));
            binding.playerChoice().ifPresent(choice -> entry.put(PLAYER_CHOICE_KEY,
                    encodePlayerChoice(binding.parameter().type(), choice)));
            encoded.add(entry);
        }
        return encoded;
    }

    private ListTag encodePackages(Map<ResourceLocation, GachaDrawPackage> packages,
                                   GachaParameterSchema schema) {
        GachaParameterNbtEncoder parameterEncoder = new GachaParameterNbtEncoder(schema, registries);
        ListTag encoded = new ListTag();
        for (GachaDrawPackage drawPackage : packages.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(ID_KEY, drawPackage.id().toString());
            entry.putString(EXECUTION_PLAN_KEY, drawPackage.executionPlan().toString());
            entry.putString(MINIMUM_DURABILITY_KEY, drawPackage.minimumDurability().name());
            entry.put(PARAMETERS_KEY, parameterEncoder.encode(drawPackage.parameters()));
            encoded.add(entry);
        }
        return encoded;
    }

    private List<GachaParameterDefinition<?>> decodeDefinitions(CompoundTag snapshot) {
        List<GachaParameterDefinition<?>> decoded = new ArrayList<>();
        ListTag definitions = snapshot.getList(DEFINITIONS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < definitions.size(); index++) {
            CompoundTag entry = definitions.getCompound(index);
            GachaParameterType<?> type = parameterTypes.resolve(ResourceLocation.parse(entry.getString(TYPE_KEY)))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown card pool parameter type: "
                            + entry.getString(TYPE_KEY)));
            decoded.add(decodeDefinition(entry, type));
        }
        return List.copyOf(decoded);
    }

    private <T> GachaParameterDefinition<T> decodeDefinition(CompoundTag encoded, GachaParameterType<T> type) {
        Optional<T> defaultValue = encoded.contains(DEFAULT_KEY)
                ? Optional.of(decodeValue(type, encoded.get(DEFAULT_KEY))) : Optional.empty();
        return new GachaParameterDefinition<>(decodeKey(encoded), type,
                GachaParameterDirection.valueOf(encoded.getString(DIRECTION_KEY)),
                encoded.getBoolean(REQUIRED_KEY), defaultValue);
    }

    private List<GachaParameterBinding<?>> decodeBindings(CompoundTag snapshot, GachaParameterSchema schema) {
        List<GachaParameterBinding<?>> decoded = new ArrayList<>();
        ListTag bindings = snapshot.getList(BINDINGS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < bindings.size(); index++) {
            CompoundTag entry = bindings.getCompound(index);
            GachaParameterDefinition<?> definition = schema.require(decodeKey(entry));
            decoded.add(decodeBinding(entry, definition));
        }
        return List.copyOf(decoded);
    }

    private <T> GachaParameterBinding<T> decodeBinding(CompoundTag encoded,
                                                       GachaParameterDefinition<T> definition) {
        Optional<T> boundValue = encoded.contains(BOUND_VALUE_KEY)
                ? Optional.of(decodeValue(definition.type(), encoded.get(BOUND_VALUE_KEY))) : Optional.empty();
        Optional<GachaPlayerChoice<T>> playerChoice = encoded.contains(PLAYER_CHOICE_KEY)
                ? Optional.of(decodePlayerChoice(encoded.getCompound(PLAYER_CHOICE_KEY), definition))
                : Optional.empty();
        return new GachaParameterBinding<>(definition,
                GachaParameterBindingSource.valueOf(encoded.getString(SOURCE_KEY)),
                GachaParameterOverridePolicy.valueOf(encoded.getString(OVERRIDE_POLICY_KEY)), boundValue,
                playerChoice);
    }

    private CompoundTag encodePlayerChoice(GachaParameterType<?> type, GachaPlayerChoice<?> choice) {
        CompoundTag encoded = new CompoundTag();
        encoded.putString(LABEL_KEY, choice.labelTranslationKey());
        if (choice.constraint() instanceof GachaBooleanChoiceConstraint) {
            encoded.putString(CHOICE_KIND_KEY, "boolean");
        } else if (choice.constraint() instanceof GachaNumberRangeChoiceConstraint<?>(
                BigDecimal minimum, BigDecimal maximum, BigDecimal step
        )) {
            encoded.putString(CHOICE_KIND_KEY, "range");
            encoded.putString(MINIMUM_KEY, minimum.toPlainString());
            encoded.putString(MAXIMUM_KEY, maximum.toPlainString());
            encoded.putString(STEP_KEY, step.toPlainString());
        } else if (choice.constraint() instanceof GachaEnumeratedChoiceConstraint<?> enumeration) {
            encoded.putString(CHOICE_KIND_KEY, "enumerated");
            ListTag values = new ListTag();
            enumeration.values().forEach(value -> values.add(encodeValue(type, value)));
            encoded.put(VALUES_KEY, values);
        } else {
            throw new IllegalArgumentException("Unsupported player choice constraint: "
                    + choice.constraint().getClass().getName());
        }
        return encoded;
    }

    private <T> GachaPlayerChoice<T> decodePlayerChoice(CompoundTag encoded,
                                                        GachaParameterDefinition<T> definition) {
        String label = encoded.getString(LABEL_KEY);
        return switch (encoded.getString(CHOICE_KIND_KEY)) {
            case "boolean" -> castPlayerChoice(new GachaPlayerChoice<>(label,
                    GachaBooleanChoiceConstraint.INSTANCE));
            case "range" -> castPlayerChoice(new GachaPlayerChoice<>(label,
                    new GachaNumberRangeChoiceConstraint<Number>(new BigDecimal(encoded.getString(MINIMUM_KEY)),
                            new BigDecimal(encoded.getString(MAXIMUM_KEY)),
                            new BigDecimal(encoded.getString(STEP_KEY)))));
            case "enumerated" -> new GachaPlayerChoice<>(label,
                    new GachaEnumeratedChoiceConstraint<>(decodeChoiceValues(encoded, definition)));
            default -> throw new IllegalArgumentException("Unknown player choice constraint kind: "
                    + encoded.getString(CHOICE_KIND_KEY));
        };
    }

    private <T> List<T> decodeChoiceValues(CompoundTag encoded, GachaParameterDefinition<T> definition) {
        List<T> values = new ArrayList<>();
        Tag storedValues = encoded.get(VALUES_KEY);
        if (!(storedValues instanceof ListTag entries)) {
            throw new IllegalArgumentException("Missing enumerated player choice values");
        }
        for (Tag entry : entries) {
            values.add(decodeValue(definition.type(), entry));
        }
        return List.copyOf(values);
    }

    private Map<ResourceLocation, GachaDrawPackage> decodePackages(CompoundTag snapshot,
                                                                   GachaParameterSchema schema) {
        GachaParameterNbtDecoder parameterDecoder = new GachaParameterNbtDecoder(schema, registries);
        Map<ResourceLocation, GachaDrawPackage> decoded = new LinkedHashMap<>();
        ListTag packages = snapshot.getList(PACKAGES_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < packages.size(); index++) {
            CompoundTag entry = packages.getCompound(index);
            ResourceLocation id = ResourceLocation.parse(entry.getString(ID_KEY));
            ResourceLocation executionPlan = ResourceLocation.parse(entry.getString(EXECUTION_PLAN_KEY));
            GachaDrawPackage drawPackage = new GachaDrawPackage(id, executionPlan,
                    parameterDecoder.decodeSet(entry.getCompound(PARAMETERS_KEY)),
                    GachaDurability.valueOf(entry.getString(MINIMUM_DURABILITY_KEY)));
            if (decoded.putIfAbsent(id, drawPackage) != null) {
                throw new IllegalArgumentException("Duplicate draw package in card pool snapshot: " + id);
            }
        }
        return decoded;
    }

    private Tag encodeValue(GachaParameterType<?> type, Object value) {
        return encodeTypedValue(type, value);
    }

    private <T> Tag encodeTypedValue(GachaParameterType<T> type, Object value) {
        if (!type.valueClass().isInstance(value)) {
            throw new IllegalArgumentException("Value does not match parameter type: " + type.id());
        }
        return type.codec().encodeStart(ops, type.valueClass().cast(value))
                .getOrThrow(IllegalArgumentException::new);
    }

    private <T> T decodeValue(GachaParameterType<T> type, Tag encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("Missing encoded value for parameter type: " + type.id());
        }
        return type.codec().parse(ops, encoded).getOrThrow(IllegalArgumentException::new);
    }
}
