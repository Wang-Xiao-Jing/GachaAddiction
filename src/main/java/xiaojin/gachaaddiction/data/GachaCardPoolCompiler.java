package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.cardpool.*;
import xiaojin.gachaaddiction.cardpool.parameter.*;
import xiaojin.gachaaddiction.cardpool.transaction.GachaConsumptionSpec;
import xiaojin.gachaaddiction.cardpool.transaction.GachaDurability;
import xiaojin.gachaaddiction.execution.*;
import xiaojin.gachaaddiction.reward.GachaRewardPool;
import xiaojin.gachaaddiction.reward.number.GachaConstantLongProvider;
import xiaojin.gachaaddiction.reward.parameter.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public final class GachaCardPoolCompiler {
    private static final Set<String> ROOT_FIELDS = Set.of("format_version", "execution_plan", "reward_pool", "parameter_bindings",
            "packages", "presentation", "availability", "delivery");
    private static final Set<String> BINDING_FIELDS = Set.of("parameter", "source", "override_policy", "value",
            "player_choice");
    private static final Set<String> PLAYER_CHOICE_FIELDS = Set.of("label", "minimum", "maximum", "step",
            "values");
    private static final Set<String> PACKAGE_FIELDS = Set.of("id", "rounds", "parameters", "consumption",
            "minimum_durability");
    private static final Set<String> CONSUMPTION_FIELDS = Set.of("type", "data");
    private static final Set<String> PRESENTATION_FIELDS = Set.of(
            "title", "description", "icon", "result_style");
    private static final Set<String> AVAILABILITY_FIELDS = Set.of("enabled", "minimum_permission_level", "opens_at",
            "closes_at");
    private static final Set<String> DELIVERY_FIELDS = Set.of("mode", "handler");

    private final RegistryOps<JsonElement> ops;

    public GachaCardPoolCompiler(HolderLookup.Provider registries) {
        this.ops = RegistryOps.create(JsonOps.INSTANCE, registries);
    }

    private static GachaCardPoolAvailability availability(JsonObject json, String location) {
        GachaJson.requireOnly(json, AVAILABILITY_FIELDS, location);
        return new GachaCardPoolAvailability(GachaJson.bool(json, "enabled", true),
                GachaJson.integer(json, "minimum_permission_level", location),
                instant(json, "opens_at", location), instant(json, "closes_at", location));
    }

    private static GachaCardPoolDelivery delivery(JsonObject json, String location) {
        GachaJson.requireOnly(json, DELIVERY_FIELDS, location);
        GachaRewardDeliveryMode mode = GachaRewardDeliveryMode.valueOf(GachaJson.string(json, "mode", location));
        Optional<ResourceLocation> handler = json.has("handler")
                ? Optional.of(ResourceLocation.parse(GachaJson.string(json, "handler", location)))
                : Optional.empty();
        return new GachaCardPoolDelivery(mode, handler);
    }

    private static Optional<Instant> instant(JsonObject json, String field, String location) {
        if (!json.has(field)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(GachaJson.string(json, field, location)));
        } catch (RuntimeException failure) {
            throw new JsonParseException("Invalid timestamp in " + location + "." + field, failure);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> GachaPlayerChoice<T> castPlayerChoice(GachaPlayerChoice<?> choice) {
        return (GachaPlayerChoice<T>) choice;
    }

    private static void requireChoiceFields(JsonObject json, Set<String> fields, String location) {
        GachaJson.requireOnly(json, fields, location);
        for (String field : fields) {
            if (!json.has(field)) {
                throw new JsonParseException("Missing field '" + field + "' in " + location);
            }
        }
    }

    private static BigDecimal decimal(JsonObject json, String field, String location) {
        JsonElement value = json.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("Field '" + field + "' must be a number in " + location);
        }
        try {
            return new BigDecimal(value.getAsString());
        } catch (NumberFormatException failure) {
            throw new JsonParseException("Invalid decimal in " + location + "." + field, failure);
        }
    }

    private static GachaExecutionPlanDefinition legacyPlan(ResourceLocation planId, ResourceLocation rewardPoolId,
                                                           GachaParameterSchema schema, int rounds,
                                                           List<GachaConsumptionSpec> consumption) {
        List<GachaParameterMappingEntry> mappings = schema.definitions().stream()
                .map(definition -> new GachaParameterMappingEntry(definition.key(),
                        new GachaDirectParameterValue(definition.key())))
                .toList();
        List<xiaojin.gachaaddiction.execution.GachaExecutionStep> steps = new ArrayList<>();
        steps.add(new GachaRewardPoolExecutionStep(GachaBuiltinExecutionSteps.REWARD_POOL, rewardPoolId,
                new GachaConstantLongProvider(rounds), new GachaParameterMapping(mappings),
                Set.of(GachaExecutionChannel.PARAMETERS, GachaExecutionChannel.PROVENANCE),
                Set.of(GachaExecutionChannel.REWARDS, GachaExecutionChannel.PARAMETERS,
                        GachaExecutionChannel.DIAGNOSTICS, GachaExecutionChannel.PROVENANCE)));
        if (!consumption.isEmpty()) {
            steps.add(new GachaConsumptionExecutionStep(GachaBuiltinExecutionSteps.CONSUMPTION, consumption,
                    Set.of(GachaExecutionChannel.PARAMETERS),
                    Set.of(GachaExecutionChannel.CONSUMPTION, GachaExecutionChannel.DIAGNOSTICS)));
        }
        return new GachaExecutionPlanDefinition(planId, schema, steps);
    }

    private static ResourceLocation generatedPlanId(ResourceLocation cardPoolId, ResourceLocation packageId) {
        return ResourceLocation.fromNamespaceAndPath(cardPoolId.getNamespace(),
                "generated/card_pool/" + cardPoolId.getPath() + "/" + packageId.getNamespace()
                        + "/" + packageId.getPath());
    }

    private static List<GachaConsumptionSpec> consumption(JsonArray encoded, String location) {
        List<GachaConsumptionSpec> consumption = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            String consumptionLocation = location + ".consumption[" + index + "]";
            JsonObject specification = requireObject(encoded.get(index), consumptionLocation);
            GachaJson.requireOnly(specification, CONSUMPTION_FIELDS, consumptionLocation);
            JsonObject data = GachaJson.object(specification, "data", consumptionLocation);
            Tag convertedData = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, data);
            if (!(convertedData instanceof CompoundTag encodedData)) {
                throw new JsonParseException("Consumption data must encode to a compound tag in "
                        + consumptionLocation);
            }
            consumption.add(new GachaConsumptionSpec(
                    ResourceLocation.parse(GachaJson.string(specification, "type", consumptionLocation)),
                    encodedData));
        }
        return List.copyOf(consumption);
    }

    private static JsonObject requireObject(JsonElement json, String location) {
        if (json == null || !json.isJsonObject()) {
            throw new JsonParseException("Card pool entry must be an object in " + location);
        }
        return json.getAsJsonObject();
    }

    public GachaCardPoolCompilation compile(
            Map<ResourceLocation, GachaRawDefinition> definitions,
            Map<ResourceLocation, GachaRewardPool> rewardPools,
            Map<ResourceLocation, GachaExecutionPlanDefinition> executionPlans) {
        Map<ResourceLocation, GachaCardPoolDefinition> compiled = new LinkedHashMap<>();
        Map<ResourceLocation, GachaExecutionPlanDefinition> generatedPlans = new LinkedHashMap<>();
        definitions.forEach((id, definition) -> compiled.put(id, compileCardPool(id, definition.data(), rewardPools,
                executionPlans, generatedPlans)));
        return new GachaCardPoolCompilation(compiled, generatedPlans);
    }

    private GachaCardPoolDefinition compileCardPool(ResourceLocation id, JsonObject json,
                                                    Map<ResourceLocation, GachaRewardPool> rewardPools,
                                                    Map<ResourceLocation, GachaExecutionPlanDefinition> executionPlans,
                                                    Map<ResourceLocation, GachaExecutionPlanDefinition> generatedPlans) {
        String location = "card pool " + id;
        GachaJson.requireOnly(json, ROOT_FIELDS, location);
        if (json.has("execution_plan") == json.has("reward_pool")) {
            throw new JsonParseException("Card pool must declare exactly one of execution_plan or reward_pool in "
                    + location);
        }
        if (json.has("execution_plan")) {
            ResourceLocation planId = ResourceLocation.parse(GachaJson.string(json, "execution_plan", location));
            GachaExecutionPlanDefinition plan = executionPlans.get(planId);
            if (plan == null) {
                throw new JsonParseException("Unknown execution plan '" + planId + "' in " + location);
            }
            return compileCardPool(id, json, planId, plan.parameterSchema(),
                    packages(planId, plan.parameterSchema(), GachaJson.array(json, "packages", location), location));
        }
        ResourceLocation rewardPoolId = ResourceLocation.parse(GachaJson.string(json, "reward_pool", location));
        GachaRewardPool rewardPool = rewardPools.get(rewardPoolId);
        if (rewardPool == null) {
            throw new JsonParseException("Unknown reward pool '" + rewardPoolId + "' in " + location);
        }
        Map<ResourceLocation, GachaDrawPackage> packages = legacyPackages(id, rewardPoolId,
                rewardPool.parameterSchema(), GachaJson.array(json, "packages", location), location, generatedPlans);
        return compileCardPool(id, json, rewardPoolId, rewardPool.parameterSchema(), packages);
    }

    private GachaCardPoolDefinition compileCardPool(ResourceLocation id, JsonObject json,
                                                    ResourceLocation parameterOwner, GachaParameterSchema schema,
                                                    Map<ResourceLocation, GachaDrawPackage> packages) {
        String location = "card pool " + id;
        List<GachaParameterBinding<?>> bindings = bindings(parameterOwner, schema,
                GachaJson.array(json, "parameter_bindings", location), location);
        GachaCardPoolPresentation presentation = presentation(
                GachaJson.object(json, "presentation", location), location + ".presentation");
        GachaCardPoolAvailability availability = availability(
                GachaJson.object(json, "availability", location), location + ".availability");
        GachaCardPoolDelivery delivery = delivery(
                GachaJson.object(json, "delivery", location), location + ".delivery");
        return new GachaCardPoolDefinition(id, packages, bindings, schema,
                presentation, availability, delivery.mode(), delivery.handler());
    }

    private GachaCardPoolPresentation presentation(JsonObject json, String location) {
        GachaJson.requireOnly(json, PRESENTATION_FIELDS, location);
        ItemStack icon = ItemStack.CODEC.parse(ops, json.get("icon")).getOrThrow(JsonParseException::new);
        GachaCardPoolResultStyle resultStyle;
        try {
            resultStyle = GachaCardPoolResultStyle.parse(GachaJson.string(json, "result_style", location));
        } catch (IllegalArgumentException failure) {
            throw new JsonParseException("Unknown card pool result style in " + location, failure);
        }
        return new GachaCardPoolPresentation(GachaJson.string(json, "title", location),
                json.has("description") ? Optional.of(GachaJson.string(json, "description", location))
                        : Optional.empty(),
                icon, resultStyle);
    }

    private List<GachaParameterBinding<?>> bindings(ResourceLocation rewardPoolId, GachaParameterSchema schema,
                                                    JsonArray encoded, String location) {
        List<GachaParameterBinding<?>> bindings = new ArrayList<>();
        for (int index = 0; index < encoded.size(); index++) {
            String bindingLocation = location + ".parameter_bindings[" + index + "]";
            JsonObject binding = requireObject(encoded.get(index), bindingLocation);
            GachaJson.requireOnly(binding, BINDING_FIELDS, bindingLocation);
            GachaParameterDefinition<?> definition = schema.require(new GachaParameterKey(rewardPoolId,
                    GachaJson.string(binding, "parameter", bindingLocation)));
            bindings.add(binding(definition, binding, bindingLocation));
        }
        return List.copyOf(bindings);
    }

    private <T> GachaParameterBinding<T> binding(GachaParameterDefinition<T> definition,
                                                 JsonObject json, String location) {
        Optional<T> boundValue = json.has("value")
                ? Optional.of(definition.type().codec().parse(ops, json.get("value"))
                .getOrThrow(JsonParseException::new)) : Optional.empty();
        Optional<GachaPlayerChoice<T>> playerChoice = json.has("player_choice")
                ? Optional.of(playerChoice(definition,
                GachaJson.object(json, "player_choice", location), location + ".player_choice"))
                : Optional.empty();
        return new GachaParameterBinding<>(definition,
                GachaParameterBindingSource.valueOf(GachaJson.string(json, "source", location)),
                GachaParameterOverridePolicy.valueOf(GachaJson.string(json, "override_policy", location)),
                boundValue, playerChoice);
    }

    private <T> GachaPlayerChoice<T> playerChoice(GachaParameterDefinition<T> definition,
                                                  JsonObject json, String location) {
        GachaJson.requireOnly(json, PLAYER_CHOICE_FIELDS, location);
        String label = GachaJson.string(json, "label", location);
        if (definition.type().id().equals(GachaParameterTypes.BOOLEAN.id())) {
            requireChoiceFields(json, Set.of("label"), location);
            return castPlayerChoice(new GachaPlayerChoice<>(label, GachaBooleanChoiceConstraint.INSTANCE));
        }
        if (definition.type().id().equals(GachaParameterTypes.INTEGER.id())
                || definition.type().id().equals(GachaParameterTypes.LONG.id())
                || definition.type().id().equals(GachaParameterTypes.DOUBLE.id())) {
            requireChoiceFields(json, Set.of("label", "minimum", "maximum", "step"), location);
            return castPlayerChoice(new GachaPlayerChoice<>(label, new GachaNumberRangeChoiceConstraint<Number>(
                    decimal(json, "minimum", location), decimal(json, "maximum", location),
                    decimal(json, "step", location))));
        }
        if (definition.type().id().equals(GachaParameterTypes.STRING.id())
                || definition.type().id().equals(GachaParameterTypes.RESOURCE_LOCATION.id())) {
            requireChoiceFields(json, Set.of("label", "values"), location);
            JsonArray values = GachaJson.array(json, "values", location);
            if (values.size() > GachaPlayerChoiceLimits.MAXIMUM_ENUMERATED_VALUES) {
                throw new JsonParseException("Too many enumerated player choice values in " + location);
            }
            List<T> decoded = new ArrayList<>();
            for (int index = 0; index < values.size(); index++) {
                decoded.add(decodeValue(definition, values.get(index), location + ".values[" + index + "]"));
            }
            return new GachaPlayerChoice<>(label, new GachaEnumeratedChoiceConstraint<>(decoded));
        }
        throw new JsonParseException("Unsupported player choice parameter type '" + definition.type().id()
                + "' in " + location);
    }

    private Map<ResourceLocation, GachaDrawPackage> packages(ResourceLocation executionPlanId,
                                                             GachaParameterSchema schema, JsonArray encoded,
                                                             String location) {
        Map<ResourceLocation, GachaDrawPackage> packages = new LinkedHashMap<>();
        for (int index = 0; index < encoded.size(); index++) {
            String packageLocation = location + ".packages[" + index + "]";
            JsonObject drawPackage = requireObject(encoded.get(index), packageLocation);
            GachaJson.requireOnly(drawPackage, Set.of("id", "parameters", "minimum_durability"), packageLocation);
            ResourceLocation packageId = ResourceLocation.parse(GachaJson.string(drawPackage, "id", packageLocation));
            GachaParameterSet parameters = drawPackage.has("parameters")
                    ? parameterValues(executionPlanId, schema,
                    GachaJson.object(drawPackage, "parameters", packageLocation), packageLocation + ".parameters")
                    : GachaParameterSet.EMPTY;
            GachaDrawPackage compiledPackage = new GachaDrawPackage(packageId, executionPlanId, parameters,
                    GachaDurability.valueOf(GachaJson.string(drawPackage, "minimum_durability", packageLocation)));
            if (packages.putIfAbsent(packageId, compiledPackage) != null) {
                throw new JsonParseException("Duplicate draw package '" + packageId + "' in " + location);
            }
        }
        return packages;
    }

    private Map<ResourceLocation, GachaDrawPackage> legacyPackages(ResourceLocation cardPoolId,
                                                                   ResourceLocation rewardPoolId,
                                                                   GachaParameterSchema schema, JsonArray encoded,
                                                                   String location,
                                                                   Map<ResourceLocation, GachaExecutionPlanDefinition>
                                                                           generatedPlans) {
        Map<ResourceLocation, GachaDrawPackage> packages = new LinkedHashMap<>();
        for (int index = 0; index < encoded.size(); index++) {
            String packageLocation = location + ".packages[" + index + "]";
            JsonObject drawPackage = requireObject(encoded.get(index), packageLocation);
            GachaJson.requireOnly(drawPackage, PACKAGE_FIELDS, packageLocation);
            ResourceLocation packageId = ResourceLocation.parse(GachaJson.string(drawPackage, "id", packageLocation));
            GachaParameterSet parameters = drawPackage.has("parameters")
                    ? parameterValues(rewardPoolId, schema,
                    GachaJson.object(drawPackage, "parameters", packageLocation), packageLocation + ".parameters")
                    : GachaParameterSet.EMPTY;
            List<GachaConsumptionSpec> consumption = drawPackage.has("consumption")
                    ? consumption(GachaJson.array(drawPackage, "consumption", packageLocation), packageLocation)
                    : List.of();
            ResourceLocation planId = generatedPlanId(cardPoolId, packageId);
            int rounds = GachaJson.integer(drawPackage, "rounds", packageLocation);
            if (rounds <= 0) {
                throw new JsonParseException("Card pool package rounds must be positive in " + packageLocation);
            }
            GachaExecutionPlanDefinition plan = legacyPlan(planId, rewardPoolId, schema, rounds, consumption);
            if (generatedPlans.putIfAbsent(planId, plan) != null) {
                throw new JsonParseException("Duplicate generated execution plan: " + planId);
            }
            GachaDrawPackage compiledPackage = new GachaDrawPackage(packageId, planId, parameters,
                    GachaDurability.valueOf(GachaJson.string(drawPackage, "minimum_durability", packageLocation)));
            if (packages.putIfAbsent(packageId, compiledPackage) != null) {
                throw new JsonParseException("Duplicate draw package '" + packageId + "' in " + location);
            }
        }
        return packages;
    }

    private GachaParameterSet parameterValues(ResourceLocation rewardPoolId, GachaParameterSchema schema,
                                              JsonObject json, String location) {
        GachaParameterSet.Builder values = GachaParameterSet.builder();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            GachaParameterDefinition<?> definition = schema.require(new GachaParameterKey(rewardPoolId,
                    entry.getKey()));
            values.put(definition, decodeValue(definition, entry.getValue(), location + "." + entry.getKey()));
        }
        return values.build();
    }

    private <T> T decodeValue(GachaParameterDefinition<T> definition, JsonElement json, String location) {
        try {
            return definition.type().codec().parse(ops, json).getOrThrow(JsonParseException::new);
        } catch (RuntimeException failure) {
            throw new JsonParseException("Invalid parameter value in " + location, failure);
        }
    }

    private record GachaCardPoolDelivery(GachaRewardDeliveryMode mode, Optional<ResourceLocation> handler) {
    }
}
