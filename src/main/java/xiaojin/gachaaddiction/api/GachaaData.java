package xiaojin.gachaaddiction.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GachaaData(
        boolean isEnable,
        boolean isInit
) {
    public static final StreamCodec<ByteBuf, GachaaData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, GachaaData::isEnable,
            ByteBufCodecs.BOOL, GachaaData::isInit,
            GachaaData::new);
}
