package xiaojin.gachaaddiction.datagen;

import net.minecraft.data.DataProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import xiaojin.gachaaddiction.GachaAddiction;

@SuppressWarnings("UnusedReturnValue")
@EventBusSubscriber(modid = GachaAddiction.MODID)
public class Datagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var completableFuture = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        buildClient(event, new ModZhCn(output));

        buildServer(event, new DatagenSoundDefinitionsProvider(output, existingFileHelper));
    }

    public static <T extends DataProvider> T buildServer(GatherDataEvent event, T provider) {
        return event.getGenerator().addProvider(event.includeServer(), provider);
    }

    public static <T extends DataProvider> T buildClient(GatherDataEvent event, T provider) {
        return event.getGenerator().addProvider(event.includeServer(), provider);
    }

    @SafeVarargs
    public static <T extends DataProvider> void buildServer(GatherDataEvent event, T... providers) {
        for (T provider : providers) {
            buildServer(event, provider);
        }
    }

    @SafeVarargs
    public static <T extends DataProvider> void buildClient(GatherDataEvent event, T... providers) {
        for (T provider : providers) {
            buildClient(event, provider);
        }
    }
}
