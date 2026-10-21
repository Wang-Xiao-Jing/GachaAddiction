package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface GachaExecutionStep {
    ResourceLocation type();

    Set<GachaExecutionChannel> reads();

    Set<GachaExecutionChannel> writes();
}
