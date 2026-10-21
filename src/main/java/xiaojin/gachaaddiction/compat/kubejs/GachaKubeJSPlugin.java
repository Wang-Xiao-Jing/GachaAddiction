package xiaojin.gachaaddiction.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class GachaKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("GachaDrawItems", GachaDrawItemKubeJS.class);
    }
}
