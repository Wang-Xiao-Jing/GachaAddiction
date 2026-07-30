package xiaojin.gachaaddiction.mixin.lootr;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 只有装了 Lootr 时才应用本包内的 mixin，否则目标类不存在会导致加载失败。
 */
public class LootrMixinPlugin implements IMixinConfigPlugin {
    private boolean lootrLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        this.lootrLoaded = FabricLoader.getInstance().isModLoaded("lootr");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return lootrLoaded;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
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
