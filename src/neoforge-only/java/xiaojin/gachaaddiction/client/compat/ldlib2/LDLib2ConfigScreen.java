package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.resource.FileResourceProvider;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.TreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.config.entry.BasicConfigEntry;
import xiaojin.gachaaddiction.generator.lang.LangHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_EMPTY;
import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_TITLE;

public class LDLib2ConfigScreen {
    private static boolean resourcesRegistered;

    public static Screen create(ModContainer modContainer, Screen parent) {
        var root = new UITemplateElement(LDLib2Templates.CONFIG_SCREEN);
        var template = root.getTemplate();
        if (template != null) {
            template.getAllStylesheets().forEach(root::addLocalStylesheet);
        }

        ScrollerView configOptionsContainer = root.select("#config_options_container", ScrollerView.class)
                .findFirst()
                .orElseThrow();
        LDLib2Layouts.constrainScrollerHeight(configOptionsContainer);
        ScrollerView breadCrumbs = root.select("#bread_crumbs", ScrollerView.class)
                .findFirst()
                .orElseThrow();
        navigationContent(root, breadCrumbs, configOptionsContainer);
        previewWindowButton(root);
        finishButton(root);

        return new ConfigScreen(modContainer, ModularUI.of(UI.of(root)), Component.translatable(GUI_CONFIG_TITLE), parent);
    }

    @SuppressWarnings("unused")
    private static void previewWindowButton(UITemplateElement root) {
        var previewWindowButton = root.select("#preview_window_button", Button.class)
                .findFirst()
                .orElseThrow();
        // TODO 打开配置界面预览小窗
    }

    private static void finishButton(UITemplateElement root) {
        var finishButton = root.select("#finish_button", Button.class)
                .findFirst()
                .orElseThrow();
        finishButton.setOnClick(event -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.onClose();
            }
        });
    }

    private static void navigationContent(UITemplateElement root, ScrollerView breadCrumbs,
                                          ScrollerView configOptionsContainer) {
        //noinspection unchecked
        TreeList<TreeNode<Component, Void>> navigationContent = root.select("#navigation_content", TreeList.class)
                .findFirst()
                .orElseThrow();
        navigationContent.setFlattenRoot(true);
        navigationContent.setStaticTree(true);
        navigationContent.setWidthFitsContent(true);
        navigationContent.setNodeUISupplier(node -> {
            var text = new TextElement().setText(node.getKey());
            text.getTextStyle().adaptiveWidth(true).adaptiveHeight(true);
            return text;
        });
        navigationContent.setSupportMultipleSelection(false);
        var entriesByNode = new IdentityHashMap<TreeNode<Component, Void>, List<BasicConfigEntry<?>>>();
        var selectedEntries = new AtomicReference<List<BasicConfigEntry<?>>>(List.of());
        var optionsSearch = new AtomicReference<>("");
        Consumer<String> filterOptions = search -> {
            optionsSearch.set(search);
            setConfigOptions(configOptionsContainer, selectedEntries.get().stream()
                    .filter(entry -> matches(entry, search))
                    .toList());
        };
        navigationContent.setOnSelectedChanged(selectedNodes -> {
            var selectedNode = selectedNodes.stream().findFirst().orElse(null);
            if (selectedNode == null || !entriesByNode.containsKey(selectedNode)) {
                selectedEntries.set(List.of());
                setBreadCrumbs(breadCrumbs, navigationContent, List.of());
                setConfigOptions(configOptionsContainer, List.of());
                return;
            }
            selectedEntries.set(entriesByNode.get(selectedNode));
            var path = new ArrayList<TreeNode<Component, Void>>();
            for (var node = selectedNode; node != null && node.getParent() != null; node = node.getParent()) {
                path.add(node);
            }
            Collections.reverse(path);
            setBreadCrumbs(breadCrumbs, navigationContent, path);
            filterOptions.accept(optionsSearch.get());
        });

        Consumer<String> rebuildTree = search -> {
            var treeRoot = new TreeNode<Component, Void>(Component.empty());
            entriesByNode.clear();
            var client = Component.translatable("neoforge.configuration.uitext.client");
            var common = Component.translatable("neoforge.configuration.uitext.common");
            var server = Component.translatable("neoforge.configuration.uitext.server");
            addConfigType(treeRoot, client, filterEntries(client, GachaAddictionConfig.clientEntries(), search), entriesByNode);
            addConfigType(treeRoot, common, filterEntries(common, GachaAddictionConfig.commonEntries(), search), entriesByNode);
            addConfigType(treeRoot, server, filterEntries(server, GachaAddictionConfig.serverEntries(), search), entriesByNode);
            if (treeRoot.getChildren().isEmpty()) {
                treeRoot.createChild(Component.translatable(GUI_CONFIG_EMPTY));
            }
            selectedEntries.set(List.of());
            setBreadCrumbs(breadCrumbs, navigationContent, List.of());
            setConfigOptions(configOptionsContainer, List.of());
            navigationContent.setRoot(treeRoot).reloadList();
            if (!search.isBlank()) {
                navigationContent.expandAllNodesIf(treeRoot, ignored -> true);
            }
        };

        var navigationSearchBox = root.select("#navigation_search_box", TextField.class)
                .findFirst()
                .orElseThrow();
        navigationSearchBox.setTextResponder(rebuildTree);

        var optionsSearchBox = root.select("#options_search_box", TextField.class)
                .findFirst()
                .orElseThrow();
        optionsSearchBox.setTextResponder(filterOptions);
        rebuildTree.accept("");
    }

    private static void setBreadCrumbs(ScrollerView breadCrumbs,
                                       TreeList<TreeNode<Component, Void>> navigationContent,
                                       List<TreeNode<Component, Void>> path) {
        breadCrumbs.clearAllScrollViewChildren();
        int pathSize = path.size();
        for (int i = 0; i < pathSize; i++) {
            var node = path.get(i);
            TextElement child = new TextElement().setText(node.getKey());
            child.getTextStyle()
                    .adaptiveHeight(true)
                    .adaptiveWidth(true);
            child.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                navigationContent.expandNodeAlongPath(node);
                navigationContent.setSelected(List.of(node), true);
            });
            breadCrumbs.addScrollViewChild(child);
            if (i >= pathSize - 1) {
                continue;
            }

            TextElement child1 = new TextElement().setText(">");
            child1.getTextStyle()
                    .textColor(0xFFE000)
                    .adaptiveHeight(true)
                    .adaptiveWidth(true);
            breadCrumbs.addScrollViewChild(child1);
        }
    }

    private static void setConfigOptions(ScrollerView configOptionsContainer,
                                         List<BasicConfigEntry<?>> entries) {
        configOptionsContainer.clearAllScrollViewChildren();
        for (var entry : entries) {
            configOptionsContainer.addScrollViewChild(ConfigEntryElementFactory.create(entry));
        }
    }

    private static List<BasicConfigEntry<?>> filterEntries(Component typeName, List<BasicConfigEntry<?>> entries,
                                                           String search) {
        if (search.isBlank() || contains(typeName.getString(), search)) {
            return entries;
        }
        return entries.stream().filter(entry -> matches(entry, search)).toList();
    }

    private static boolean matches(BasicConfigEntry<?> entry, String search) {
        if (search.isBlank() || contains(Component.translatable(entry.translationKey()).getString(), search)
                || contains(String.join(".", entry.path()), search)
                || contains(Component.translatable(entry.translationKey() + ".tooltip").getString(), search)) {
            return true;
        }
        for (int i = 1; i < entry.path().size(); i++) {
            var translationKey = LangHandler.getConfigTranslation(GachaAddiction.MODID,
                    entry.path().subList(0, i).toArray(String[]::new));
            if (contains(Component.translatable(translationKey).getString(), search)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(String value, String search) {
        return value.toLowerCase(Locale.ROOT).contains(search.strip().toLowerCase(Locale.ROOT));
    }

    public static void registerResources() {
        if (resourcesRegistered) {
            return;
        }

        var resources = UIResource.INSTANCE.getResourceInstance();
        FileResourceProvider<UITemplate> provider = new FileResourceProvider<>(resources,
                Platform.getGamePath().resolve("ldlib2/assets/gachaaddiction").toFile());
        provider.setName(GachaAddiction.MODID);
        resources.addBuiltinProvider(provider);
        resourcesRegistered = true;
    }

    private static void addConfigType(TreeNode<Component, Void> root, Component name, List<BasicConfigEntry<?>> entries,
                                      Map<TreeNode<Component, Void>, List<BasicConfigEntry<?>>> entriesByNode) {
        if (entries.isEmpty()) {
            return;
        }
        var typeNode = root.createChild(name);
        for (var entry : entries) {
            var directoryNode = typeNode;
            for (int i = 1; i < entry.path().size(); i++) {
                var directory = entry.path().subList(0, i);
                var translationKey = LangHandler.getConfigTranslation(GachaAddiction.MODID,
                        directory.toArray(String[]::new));
                directoryNode = directoryNode.getOrCreateChild(Component.translatable(translationKey));
            }
            entriesByNode.computeIfAbsent(directoryNode, ignored -> new ArrayList<>()).add(entry);
        }
    }

    private static class ConfigScreen extends ModularUIScreen {
        private final Screen parent;

        @SuppressWarnings("unused")
        private ConfigScreen(ModContainer modContainer, ModularUI modularUI, Component title, Screen parent) {
            super(modularUI, title);
            this.parent = parent;
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }

        @Override
        public void renderBackground(
                @NotNull GuiGraphics guiGraphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            if (this.minecraft != null && this.minecraft.level == null) {
                this.renderPanorama(guiGraphics, partialTick);
            }

            this.renderBlurredBackground(partialTick);
            this.renderMenuBackground(guiGraphics);
        }
    }
}
