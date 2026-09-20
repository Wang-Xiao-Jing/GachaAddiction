package xiaojin.gachaaddiction.generator;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.generator.lang.BasicLangGenerator;
import xiaojin.gachaaddiction.generator.lang.LangHandler;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnusedReturnValue")
@EventBusSubscriber(modid = GachaAddiction.MODID)
public class Datagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> completableFuture = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            TranslationKeys.init();
            addProvider(generator, new BasicLangGenerator(output, GachaAddiction.MODID, LangHandler.ZH_CN));
            addProvider(generator, new BasicLangGenerator(output, GachaAddiction.MODID, LangHandler.EN_US));
            addProvider(generator, new BasicLangGenerator(output, GachaAddiction.MODID, LangHandler.RU_RU));
        }

        if (event.includeServer()) {
            addProvider(generator, new DatagenSoundDefinitionsProvider(output, existingFileHelper));
        }
    }

    private static <T extends DataProvider> T addProvider(DataGenerator generator, T provider) {
        return generator.addProvider(true, provider);
    }
}
