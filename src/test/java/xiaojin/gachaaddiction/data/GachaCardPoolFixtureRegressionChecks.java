package xiaojin.gachaaddiction.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import xiaojin.gachaaddiction.cardpool.parameter.GachaCardPoolParameterResolver;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterBinding;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterBindingContext;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterBindingSource;
import xiaojin.gachaaddiction.cardpool.parameter.GachaParameterOverridePolicy;
import xiaojin.gachaaddiction.cardpool.parameter.GachaRequestAuthority;
import xiaojin.gachaaddiction.cardpool.parameter.IGachaParameterStateReader;
import xiaojin.gachaaddiction.execution.GachaBuiltinExecutionSteps;
import xiaojin.gachaaddiction.execution.GachaExecutionPlanDefinition;
import xiaojin.gachaaddiction.execution.GachaExecutionStepTypeRegistry;
import xiaojin.gachaaddiction.execution.GachaRewardPoolExecutionStep;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterDefinition;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterKey;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypeRegistry;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class GachaCardPoolFixtureRegressionChecks {
    private static final List<String> FIXTURES = List.of(
            "src/main/resources/data/gacha_test/gacha/card_pools/secondary_demo.json",
            "log/test-packs/secondary-interactions/data/gacha_test/gacha/card_pools/secondary_demo.json",
            "log/test-packs/custom-draw-items/data-pack/data/gacha_demo/gacha/card_pools/custom_items.json");

    public static void main(String[] args) throws IOException {
        HolderLookup.Provider registries = HolderLookup.Provider.create(Stream.empty());
        GachaExecutionStepTypeRegistry steps = new GachaExecutionStepTypeRegistry();
        GachaBuiltinExecutionSteps.register(steps);
        GachaExecutionPlanCompiler compiler = new GachaExecutionPlanCompiler(
                new GachaParameterDefinitionJson(new GachaParameterTypeRegistry(), registries), steps);
        Path root = Path.of(System.getProperty("user.dir"));
        for (String fixture : FIXTURES) {
            checkFixture(root, fixture, registries, compiler);
        }
        System.out.println("Card pool fixture regression checks passed");
    }

    private static void checkFixture(Path root, String fixture, HolderLookup.Provider registries,
                                     GachaExecutionPlanCompiler compiler) throws IOException {
        JsonObject cardPool = read(root.resolve(fixture));
        JsonObject encodedPlan = read(root.resolve(fixture.replace("/card_pools/", "/execution_plans/")));
        ResourceLocation planId = ResourceLocation.parse(cardPool.get("execution_plan").getAsString());
        GachaExecutionPlanDefinition plan = compiler.compile(Map.of(planId,
                new GachaRawDefinition(planId, 1, encodedPlan))).get(planId);
        require(plan.steps().size() == 1 && plan.steps().getFirst() instanceof GachaRewardPoolExecutionStep,
                "Fixture must contain one reward pool step: " + fixture);
        GachaRewardPoolExecutionStep step = (GachaRewardPoolExecutionStep) plan.steps().getFirst();
        require(step.target().equals(planId), "Fixture reward pool target changed: " + fixture);
        GachaParameterDefinition<?> rounds = plan.parameterSchema().require(new GachaParameterKey(planId, "rounds"));
        JsonObject bindingJson = cardPool.getAsJsonArray("parameter_bindings").get(0).getAsJsonObject();
        require(bindingJson.get("parameter").getAsString().equals("rounds"), "Missing rounds binding");
        require(bindingJson.get("override_policy").getAsString().equals("PACKAGE_VALUE"),
                "Fixture must protect rounds from request overrides");
        require(cardPool.getAsJsonArray("packages").size() == 2, "Fixture package count changed");
        for (var entry : cardPool.getAsJsonArray("packages")) {
            JsonObject drawPackage = entry.getAsJsonObject();
            ResourceLocation packageId = ResourceLocation.parse(drawPackage.get("id").getAsString());
            long expected = switch (packageId.getPath()) {
                case "single" -> 1L;
                case "triple" -> 3L;
                default -> throw new AssertionError("Unexpected fixture package: " + packageId);
            };
            GachaParameterSet packageValues = GachaParameterSet.builder().put(rounds,
                    drawPackage.getAsJsonObject("parameters").get("rounds").getAsLong()).build();
            GachaParameterSet requestValues = GachaParameterSet.builder().put(rounds, 99L).build();
            var resolved = new GachaCardPoolParameterResolver().resolve(List.of(binding(rounds, bindingJson)),
                    plan.parameterSchema(), packageValues, requestValues, GachaRequestAuthority.PLAYER,
                    new GachaParameterBindingContext(planId, UUID.randomUUID()), noState(),
                    (schema, parameters) -> "fixture");
            long actual = step.rounds().get(new GachaNumberContext(resolved.parameters(),
                    resolved.parameters(), RandomSource.create(0L)));
            require(actual == expected, "Fixture rounds changed: " + fixture + " -> " + packageId);
        }
        JsonObject unsupported = cardPool.deepCopy();
        unsupported.remove("execution_plan");
        unsupported.addProperty("reward_pool", planId.toString());
        try {
            new GachaCardPoolCompiler(registries).compile(Map.of(planId,
                    new GachaRawDefinition(planId, 1, unsupported)), Map.of(planId, plan));
        } catch (JsonParseException failure) {
            require(failure.getMessage().contains("reward_pool"), "Unexpected legacy-format rejection");
            return;
        }
        throw new AssertionError("Legacy reward_pool card pool was accepted");
    }

    private static <T> GachaParameterBinding<T> binding(GachaParameterDefinition<T> parameter, JsonObject json) {
        T value = parameter.type().codec().parse(JsonOps.INSTANCE, json.get("value")).getOrThrow();
        return new GachaParameterBinding<>(parameter,
                GachaParameterBindingSource.valueOf(json.get("source").getAsString()),
                GachaParameterOverridePolicy.valueOf(json.get("override_policy").getAsString()),
                Optional.of(value), Optional.empty());
    }

    private static IGachaParameterStateReader noState() {
        return new IGachaParameterStateReader() {
            @Override
            public <T> Optional<T> read(GachaParameterBinding<T> binding, GachaParameterBindingContext context) {
                throw new AssertionError("Constant fixture binding must not access persistent state");
            }
        };
    }

    private static JsonObject read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

