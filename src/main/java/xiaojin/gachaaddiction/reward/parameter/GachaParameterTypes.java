package xiaojin.gachaaddiction.reward.parameter;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.GachaConstants;

public final class GachaParameterTypes {
    public static final GachaParameterType<Boolean> BOOLEAN = type("boolean", Boolean.class, Codec.BOOL);
    public static final GachaParameterType<Integer> INTEGER = type("integer", Integer.class, Codec.INT);
    public static final GachaParameterType<Long> LONG = type("long", Long.class, Codec.LONG);
    public static final GachaParameterType<Double> DOUBLE = type("double", Double.class, Codec.DOUBLE);
    public static final GachaParameterType<String> STRING = type("string", String.class, Codec.STRING);
    public static final GachaParameterType<ResourceLocation> RESOURCE_LOCATION = type("resource_location",
            ResourceLocation.class, ResourceLocation.CODEC);
    public static final GachaParameterType<CompoundTag> COMPOUND_TAG = new GachaParameterType<>(id("compound_tag"),
            CompoundTag.class, CompoundTag.CODEC, CompoundTag::copy);

    private GachaParameterTypes() {
    }

    private static <T> GachaParameterType<T> type(String path, Class<T> valueClass, Codec<T> codec) {
        return new GachaParameterType<>(id(path), valueClass, codec, value -> value);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, path);
    }
}
