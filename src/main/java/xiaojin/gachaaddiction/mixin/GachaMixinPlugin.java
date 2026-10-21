package xiaojin.gachaaddiction.mixin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GachaMixinPlugin implements IMixinConfigPlugin {
    private static final String COMPAT_PREFIX = "xiaojin.gachaaddiction.mixin.compat.";
    private static final Map<String, String> REQUIRED_MODS = Map.of(
            "ftb", "ftbquests",
            "lootr", "lootr",
            "treasurebags", "treasurebags",
            "lootbundles", "lootbundles");

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(COMPAT_PREFIX)) {
            return true;
        }

        String integration = mixinClassName.substring(COMPAT_PREFIX.length()).split("\\.", 2)[0];
        String modId = REQUIRED_MODS.get(integration);
        return modId != null && FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
