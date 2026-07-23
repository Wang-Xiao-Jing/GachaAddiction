package xiaojin.gachaaddiction;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import xiaojin.gachaaddiction.client.gui.config.CustomConfigEntries;
import xiaojin.gachaaddiction.init.GachaTypes;

@Mod(value = GachaAddiction.MODID, dist = Dist.CLIENT)
public final class GachaAddictionClient {
    public GachaAddictionClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        CustomConfigEntries.register(GachaAddictionConfig.CLIENT.defaultGachaaType.getPath().getLast(), (source, target, key) ->
                CycleButton.builder((String id) -> Component.translatable(GachaTypes.getDefault(ResourceLocation.parse(id)).getTranslationKey()))
                        .withValues(GachaTypes.getLiat().stream()
                                .map(t -> t.getId().toString())
                                .toList())
                        .withInitialValue(source.get())
                        .displayOnlyValue()
                        .create(0, 0, 150, 20, Component.literal(""), (btn, newId) -> {
                            target.accept(newId);
                        }));
    }
}
