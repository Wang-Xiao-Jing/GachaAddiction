package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import net.minecraft.network.chat.Component;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.BasicConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.DoubleConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.FilterConditionConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.FilterRuleConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.IntConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.ListConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.LongConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.NumberConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.SelectorConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.StringConfigEntryElement;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.SwitchConfigEntryElement;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.config.entry.BasicConfigEntry;
import xiaojin.gachaaddiction.config.entry.BooleanConfigEntry;
import xiaojin.gachaaddiction.config.entry.ConfigEntry;
import xiaojin.gachaaddiction.config.entry.DoubleConfigEntry;
import xiaojin.gachaaddiction.config.entry.FilterListConfigEntry;
import xiaojin.gachaaddiction.config.entry.IntConfigEntry;
import xiaojin.gachaaddiction.config.entry.LongConfigEntry;
import xiaojin.gachaaddiction.config.entry.RangedNumberConfigEntry;
import xiaojin.gachaaddiction.config.entry.SelectorConfigEntry;
import xiaojin.gachaaddiction.config.entry.StringConfigEntry;
import xiaojin.gachaaddiction.config.entry.StringListConfigEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;
import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_FILTER_RULE_TITLE;
import static xiaojin.gachaaddiction.util.TranslationKeys.GUI_CONFIG_UNSUPPORTED;

public final class ConfigEntryElementFactory {
    private ConfigEntryElementFactory() {
    }

    public static BasicConfigEntryElement<?> create(BasicConfigEntry<?> configEntry) {
        BasicConfigEntryElement<?> element;
        Runnable refresh;
        switch (configEntry) {
            case BooleanConfigEntry entry -> {
                var switchElement = new SwitchConfigEntryElement();
                element = switchElement;
                refresh = () -> switchElement.setValue(entry.get(), false);
                refresh.run();
                switchElement.setOnValueChanged(next -> update(switchElement, entry, next));
            }
            case IntConfigEntry entry -> {
                var field = new IntConfigEntryElement(entry.min(), entry.max());
                element = field;
                refresh = bindNumber(field, entry);
            }
            case LongConfigEntry entry -> {
                var field = new LongConfigEntryElement(entry.min(), entry.max());
                element = field;
                refresh = bindNumber(field, entry);
            }
            case DoubleConfigEntry entry -> {
                var field = new DoubleConfigEntryElement(entry.min(), entry.max());
                element = field;
                refresh = bindNumber(field, entry);
            }
            case SelectorConfigEntry<?> entry -> {
                var selector = new SelectorConfigEntryElement<String>();
                element = selector;
                refresh = bindSelector(selector, entry);
            }
            case StringConfigEntry entry -> {
                var field = new StringConfigEntryElement();
                element = field;
                refresh = bindString(field, entry);
            }
            case StringListConfigEntry entry -> {
                var list = new ListConfigEntryElement<String>();
                element = list;
                list.setValueValidator(entry::isValid)
                        .setNewValueSupplier(() -> "")
                        .setEntryFactory(value -> {
                            var field = new StringConfigEntryElement();
                            field.getValueElement().setTextValidator(text -> entry.tryParse(text).isPresent());
                            return field;
                        });
                refresh = () -> list.setValue(entry.get(), false);
                refresh.run();
                list.setOnValueChanged(next -> update(list, entry, next));
            }
            case FilterListConfigEntry entry -> {
                var list = new ListConfigEntryElement<List<String>>();
                element = list;
                list.setValueValidator(entry::isValid)
                        .setNewValueSupplier(ArrayList::new)
                        .setEntryFactory(rule -> createFilterRuleElement(entry));
                refresh = () -> list.setValue(entry.get(), false);
                refresh.run();
                list.setOnValueChanged(next -> update(list, entry, next));
            }
            case ConfigEntry<?> entry -> {
                var field = new StringConfigEntryElement();
                element = field;
                refresh = bindConfigEntry(field, entry);
            }
            case null, default -> {
                var unsupported = new StringConfigEntryElement();
                unsupported.setValue(Component.translatable(GUI_CONFIG_UNSUPPORTED,
                        configEntry == null ? "null" : configEntry.getClass().getSimpleName()).getString());
                unsupported.getValueElement().setTextValidator(ignored -> false);
                return unsupported;
            }
        }
        configureTemplate(element, configEntry, refresh);
        return element;
    }

    private static FilterRuleConfigEntryElement createFilterRuleElement(FilterListConfigEntry entry) {
        var conditions = new FilterRuleConfigEntryElement(entry::isRuleValid);
        conditions.setText(Component.translatable(GUI_CONFIG_FILTER_RULE_TITLE));
        conditions.setValueValidator(entry::isRuleValid)
                .setNewValueSupplier(() -> "")
                .setEntryFactory(value -> {
                    var field = new FilterConditionConfigEntryElement();
                    field.setTextValidator(entry::isConditionValid);
                    field.refreshValidationState();
                    return field;
                });
        return conditions;
    }

    private static <N extends Number & Comparable<N>> Runnable bindNumber(
            NumberConfigEntryElement<N> field, RangedNumberConfigEntry<N> entry) {
        TextField value = field.getValueElement();
        Runnable refresh = () -> value.setText(entry.get().toString(), false);
        refresh.run();
        value.setTextResponder(ignored -> updateField(field, entry));
        saveOnCommit(value, entry);
        return refresh;
    }

    private static Runnable bindString(StringConfigEntryElement field, StringConfigEntry entry) {
        TextField value = field.getValueElement();
        value.setTextValidator(entry::isValid);
        Runnable refresh = () -> value.setText(entry.get(), false);
        refresh.run();
        value.setTextResponder(ignored -> updateField(field, entry));
        saveOnCommit(value, entry);
        return refresh;
    }

    private static void configureTemplate(BasicConfigEntryElement<?> element,
                                          BasicConfigEntry<?> entry, Runnable refresh) {
        TextElement description = element.select("#describe", TextElement.class).findFirst().orElseThrow();
        description.style(style -> style.tooltips(entry.translationKey() + ".tooltip"));
        element.select("#reset_button", Button.class).findFirst().orElseThrow().setOnClick(event -> {
            if (entry.reset()) {
                GachaAddictionConfig.save(entry);
                refresh.run();
                refreshEntryState(element, entry);
            }
        });
        refreshEntryState(element, entry);
    }

    private static void refreshEntryState(BasicConfigEntryElement<?> element, BasicConfigEntry<?> entry) {
        boolean changed = !Objects.equals(entry.get(), entry.defaultValue());
        var title = Component.translatable(entry.translationKey());
        if (changed) {
            title.append("*");
        }
        element.setText(title);
        element.select("#reset_button", Button.class).findFirst().orElseThrow()
                .setVisible(changed)
                .setDisplay(changed);
    }

    private static <V> Runnable bindSelector(SelectorConfigEntryElement<String> element,
                                             SelectorConfigEntry<V> entry) {
        element.setCandidates(entry.allowedValues().stream().map(entry::encode).toList());
        Runnable refresh = () -> element.setValue(entry.encode(entry.get()), false);
        refresh.run();
        element.setOnValueChanged(value -> entry.tryParse(value).ifPresent(next -> update(element, entry, next)));
        return refresh;
    }

    private static <V> Runnable bindConfigEntry(StringConfigEntryElement element, ConfigEntry<V> entry) {
        TextField value = element.getValueElement();
        value.setTextValidator(text -> entry.tryParse(text).isPresent());
        Runnable refresh = () -> value.setText(entry.encode(entry.get()), false);
        refresh.run();
        value.setTextResponder(text -> entry.tryParse(element.getValue())
                .ifPresent(next -> updateInMemory(element, entry, next)));
        saveOnCommit(value, entry);
        return refresh;
    }

    private static <V> void updateField(BasicConfigEntryElement<V> element, BasicConfigEntry<V> entry) {
        try {
            V value = element.getValue();
            if (entry.isValid(value)) {
                updateInMemory(element, entry, value);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private static <V> void updateInMemory(BasicConfigEntryElement<?> element,
                                           BasicConfigEntry<V> entry, V value) {
        if (entry.set(value)) {
            refreshEntryState(element, entry);
        }
    }

    private static void saveOnCommit(TextField field, BasicConfigEntry<?> entry) {
        field.addEventListener(UIEvents.BLUR, event -> GachaAddictionConfig.save(entry));
        field.addEventListener(UIEvents.KEY_DOWN, event -> {
            if (event.keyCode == GLFW_KEY_ENTER || event.keyCode == GLFW_KEY_KP_ENTER) {
                GachaAddictionConfig.save(entry);
            }
        });
    }

    private static <V> void update(BasicConfigEntryElement<?> element, BasicConfigEntry<V> entry, V value) {
        if (entry.isValid(value) && entry.set(value)) {
            GachaAddictionConfig.save(entry);
            refreshEntryState(element, entry);
        }
    }
}
