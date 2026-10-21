package xiaojin.gachaaddiction.generator;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.generator.lang.BasicLangGenerator;
import xiaojin.gachaaddiction.registry.language.GachaTranslations;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnusedReturnValue")
public class Datagen {
    public static void registerProviders(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> completableFuture = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            TranslationKeys.init();
            addProvider(generator, new BasicLangGenerator(output, GachaConstants.MODID, GachaTranslations.ZH_CN));
            addProvider(generator, new BasicLangGenerator(output, GachaConstants.MODID, GachaTranslations.EN_US));
            addProvider(generator, new BasicLangGenerator(output, GachaConstants.MODID, GachaTranslations.RU_RU));
            addProvider(generator, new DatagenItemModel(output, GachaConstants.MODID, existingFileHelper));
        }

        if (event.includeServer()) {
            addProvider(generator, new DatagenSoundDefinitionsProvider(output, existingFileHelper));
        }
    }

    private static <T extends DataProvider> T addProvider(DataGenerator generator, T provider) {
        return generator.addProvider(true, provider);
    }
}
