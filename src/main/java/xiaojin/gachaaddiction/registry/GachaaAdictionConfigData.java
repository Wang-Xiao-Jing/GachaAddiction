package xiaojin.gachaaddiction.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.init.ModSoundEvents;
import xiaojin.gachaaddiction.util.GachaaPatterns;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

public class GachaaAdictionConfigData extends SimpleJsonResourceReloadListener<JsonElement> {
    // 规则本体见 GachaaPatterns，那边不依赖客户端类
    public static final Pattern TAG_PATTERN = GachaaPatterns.TAG_PATTERN;
    public static final Pattern ID_PATTERN = GachaaPatterns.ID_PATTERN;
    public static final Pattern OPERATOR_RARITY_PATTERN = GachaaPatterns.OPERATOR_RARITY_PATTERN;
    public static final Pattern RARITY_PATTERN = GachaaPatterns.RARITY_PATTERN;
    public static final GachaaAdictionConfigData INSTANCE = new GachaaAdictionConfigData();
    private static final SimpleSoundInstance DEFAULT_SIMPLE_SOUND_INSTANCE = SimpleSoundInstance.forUI(ModSoundEvents.LEVEL0.value(), 1.0f, 2.0f);
    private static SimpleSoundInstance defaultRewardsSounnd;
    private static Function<ItemStack, SimpleSoundInstance> soundEventsFunction;

    public GachaaAdictionConfigData() {
        // 26.2 起改为 Codec + FileToIdConverter，不再接收 Gson
        super(ExtraCodecs.JSON, FileToIdConverter.json("gachaaadiction"));
    }

    public static Function<ItemStack, SimpleSoundInstance> getSoundEventsFunction() {
        return soundEventsFunction;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        object.forEach((k, v) -> {
            String path = k.getPath();
            if (path.equals("reward_sounnd")) {
                rewardSounnd(v, soundManager, profiler);
            }
        });
    }

    private void rewardSounnd(JsonElement jsonElement, SoundManager soundManager, ProfilerFiller profiler) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        defaultRewardsSounnd = createDefaultSimpleSoundInstance(soundManager, jsonObject, profiler);
        soundEventsFunction = createSimpleSoundInstanceFunction(soundManager, jsonObject, profiler);
    }

    private Function<ItemStack, SimpleSoundInstance> createSimpleSoundInstanceFunction(SoundManager soundManager, JsonObject jsonObject, ProfilerFiller profiler) {
        if (!jsonObject.has("match")) {
            return itemStack -> defaultRewardsSounnd;
        }

        Map<Item, SimpleSoundInstance> itemMap = new HashMap<>();
        Map<TagKey<Item>, SimpleSoundInstance> tagKeyMap = new HashMap<>();
        TreeMap<Integer, SimpleSoundInstance> levelMap = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.getAsJsonObject("match").asMap().entrySet()) {
            String k = entry.getKey();
            JsonElement value = entry.getValue();
            if (k == null || k.isEmpty()) {
                continue;
            }

            SimpleSoundInstance simpleSoundInstance = createSimpleSoundInstance(soundManager, value);
            if (simpleSoundInstance == null) {
                continue;
            }

            if (k.startsWith("#") && TAG_PATTERN.matcher(k).matches()) {
                Identifier key = Identifier.parse(k.substring(1));
                tagKeyMap.put(TagKey.create(Registries.ITEM, key), simpleSoundInstance);
                continue;
            }

            if (RARITY_PATTERN.matcher(k).matches()) {
                levelMap.put(Integer.parseInt(k), simpleSoundInstance);
                continue;
            }

            if (ID_PATTERN.matcher(k).matches()) {
                Identifier key = Identifier.parse(k);
                Item item = BuiltInRegistries.ITEM.getValue(key);
                if (item == Items.AIR) {
                    continue;
                }
                itemMap.put(item, simpleSoundInstance);
            }
        }

        return itemStack -> {
            SimpleSoundInstance simpleSoundInstance = itemMap.get(itemStack.getItem());
            if (simpleSoundInstance != null) {
                return simpleSoundInstance;
            }
            for (var v : tagKeyMap.entrySet()) {
                if (itemStack.is(v.getKey())) {
                    return v.getValue();
                }
            }

            int level = RarityUtil.getRarityLevel(itemStack);
            Integer floorKey = levelMap.floorKey(level);
            if (floorKey != null) {
                return levelMap.get(floorKey);
            }

            return defaultRewardsSounnd;
        };
    }

    private static boolean hasSound(SoundManager soundManager, Identifier v) {
        return soundManager.getSoundEvent(v) != null;
    }

    private static SimpleSoundInstance createDefaultSimpleSoundInstance(SoundManager soundManager, JsonObject jsonObject, ProfilerFiller profiler) {
        if (!jsonObject.has("default")) {
            return DEFAULT_SIMPLE_SOUND_INSTANCE;
        }
        SimpleSoundInstance instance = createSimpleSoundInstance(soundManager, jsonObject.get("default"));
        if (instance == null) {
            return DEFAULT_SIMPLE_SOUND_INSTANCE;
        }
        return instance;
    }

    private static SimpleSoundInstance createSimpleSoundInstance(Identifier location, float pitch, float volume) {
        return new SimpleSoundInstance(location, SoundSource.MASTER, volume, pitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    @Nullable
    private static SimpleSoundInstance createSimpleSoundInstance(SoundManager soundManager, JsonElement element) {
        if (!element.isJsonObject()) {
            Identifier id = Identifier.parse(element.getAsString());
            if (!hasSound(soundManager, id)) {
                GachaAddiction.LOGGER.error("未找到id");
                return null;
            }
            return createSimpleSoundInstance(id, 1.0f, 2.0f);
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has("id")) {
            GachaAddiction.LOGGER.error("未找到id");
            return null;
        }

        Identifier id = Identifier.parse(object.get("id").getAsString());
        if (!hasSound(soundManager, id)) {
            GachaAddiction.LOGGER.error("未找到id为 {} 的音效", id);
            return null;
        }

        float pitch = 1.0f;
        if (object.has("pitch")) {
            pitch = object.get("pitch").getAsFloat();
        }

        float volume = 2.0f;
        if (object.has("volume")) {
            volume = object.get("volume").getAsFloat();
        }

        return createSimpleSoundInstance(id, pitch, volume);
    }
}
