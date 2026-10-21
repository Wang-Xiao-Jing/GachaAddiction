package xiaojin.gachaaddiction.execution;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface IGachaExecutionStep {
    ResourceLocation type();

    Set<GachaExecutionChannel> reads();

    Set<GachaExecutionChannel> writes();
}
