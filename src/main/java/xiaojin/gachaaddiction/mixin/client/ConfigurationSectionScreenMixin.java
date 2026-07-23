package xiaojin.gachaaddiction.mixin.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Element;
import net.neoforged.neoforge.common.ModConfigSpec.Range;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xiaojin.gachaaddiction.client.gui.config.CustomConfigEntries;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(ConfigurationSectionScreen.class)
public abstract class ConfigurationSectionScreenMixin {

    @Shadow
    protected abstract MutableComponent getTranslationComponent(String key);

    @Shadow
    protected abstract Component getTooltipComponent(String key, Range<?> range);

    @Shadow
    protected abstract void onChanged(String key);

    @Inject(method = "createStringValue", remap = false, at = @At("HEAD"), cancellable = true)
    private void gachaaddiction$onCreateStringValue(String key, Predicate<String> tester,
                                                    Supplier<String> source, Consumer<String> target,
                                                    CallbackInfoReturnable<Element> ci) {
        var factory = CustomConfigEntries.get(key);
        if (factory == null) return;

        AbstractWidget widget = factory.create(source, this.gachaaddiction$newTarget(target, key), key);
        ci.setReturnValue(new Element(
                getTranslationComponent(key),
                getTooltipComponent(key, null),
                widget,
                false));
    }

    @Unique
    private Consumer<String> gachaaddiction$newTarget(Consumer<String> target, String key) {
        return newValue -> {
            target.accept(newValue);
            onChanged(key);
        };
    }
}
