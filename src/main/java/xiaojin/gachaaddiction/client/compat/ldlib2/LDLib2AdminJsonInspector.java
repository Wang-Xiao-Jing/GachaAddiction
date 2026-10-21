package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.ui.*;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.admin.GachaDefinitionDocumentKey;
import xiaojin.gachaaddiction.client.GachaLocalDocumentState;
import xiaojin.gachaaddiction.client.GachaLocalWorkspace;
import xiaojin.gachaaddiction.data.GachaDefinitionKind;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2AdminJsonUiConstants.ValueType;

public class LDLib2AdminJsonInspector implements IConfigurable {
    private static final Gson JSON = new GsonBuilder().serializeNulls().disableHtmlEscaping()
            .setPrettyPrinting().create();
    private static final String NUMBER_PATTERN = "-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";
    private final LDLib2LocalDefinitionEditor editor;
    private final GachaLocalDocumentState state;
    private @Nullable JsonElement root;
    private @Nullable String observedContent;
    private @Nullable GachaDefinitionDocumentKey observedKey;
    private @Nullable ConfiguratorGroup inspectedGroup;
    private final List<InputControl> inputs = new ArrayList<>();
    private final List<ArrayConfiguratorGroup<JsonItem>> arrays = new ArrayList<>();
    private final List<ReferenceControl> references = new ArrayList<>();
    private final Set<List<String>> invalidPaths = new HashSet<>();
    private Component error = Component.empty();
    private boolean rawValid;
    private boolean pendingRebuild;
    private boolean rejectedMutation;

    public LDLib2AdminJsonInspector(LDLib2LocalDefinitionEditor editor) {
        this.editor = editor;
        state = editor.state();
    }

    public void acceptRawContent(String content) {
        if (!state.canEditDocument()) return;
        if (!invalidPaths.isEmpty()) pendingRebuild = true;
        invalidPaths.clear();
        state.setContent(content);
        parseContent(content);
    }

    public Component error() {
        return !invalidPaths.isEmpty() && error.getString().isEmpty()
                ? Component.translatable(TranslationKeys.GUI_ADMIN_JSON_INVALID_VALUE) : error;
    }

    public void refresh() {
        var document = state.document();
        var key = document == null ? null : document.key();
        if (!Objects.equals(key, observedKey)) {
            observedKey = key;
            observedContent = null;
            root = null;
            invalidPaths.clear();
            pendingRebuild = true;
        }
        if (!Objects.equals(state.content(), observedContent)) parseContent(state.content());
        if (pendingRebuild && rawValid && root != null) {
            pendingRebuild = false;
            inputs.clear();
            arrays.clear();
            references.clear();
            inspectedGroup = editor.inspectorView.inspect(this);
            if (rejectedMutation) {
                rejectedMutation = false;
                invalidPaths.remove(List.of());
            }
        }
        boolean editable = rawValid && root != null && state.canEditDocument();
        inputs.removeIf(input -> !attached(input.element()));
        inputs.forEach(input -> input.element().setActive(editable && (!input.structural() || invalidPaths.isEmpty())));
        references.removeIf(reference -> !attached(reference.configurator()));
        references.forEach(reference -> {
            GachaDefinitionKind kind = referenceKind(reference.path());
            reference.configurator().setDisplay(kind != null);
            List<String> candidates = kind == null ? List.of() : state.workspace().candidates(kind);
            if (!reference.configurator().selector.getCandidates().equals(candidates)) {
                reference.configurator().selector.setCandidates(candidates);
            }
        });
        arrays.removeIf(array -> !attached(array));
        arrays.forEach(array -> {
            boolean mutable = editable && invalidPaths.isEmpty();
            array.setCanAdd(mutable).setCanRemove(mutable).setCanReorder(mutable);
            array.removeButton.setActive(mutable && array.getSelected() != null);
        });
        updateValidity();
    }

    private void parseContent(String content) {
        JsonElement previous = root;
        observedContent = content;
        root = null;
        rawValid = false;
        error = Component.empty();
        try {
            if (!withinLimits(content)) {
                error = Component.translatable(TranslationKeys.GUI_ADMIN_JSON_LIMIT,
                        LDLib2AdminJsonUiConstants.MAXIMUM_DEPTH, LDLib2AdminJsonUiConstants.MAXIMUM_NODES);
            } else {
                root = JsonParser.parseString(content);
                rawValid = true;
                if (previous == null || !sameStructure(previous, root)) pendingRebuild = true;
            }
        } catch (IOException | RuntimeException failure) {
            error = Component.translatable(TranslationKeys.GUI_ADMIN_JSON_INVALID,
                    failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage());
        }
        if (!rawValid) root = previous;
        updateValidity();
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        if (root == null || observedKey == null) return;
        if (root.isJsonObject()) buildObject(father, () -> root, this::commitRoot, List.of());
        else father.addConfigurator(buildValue(Component.translatable(TranslationKeys.GUI_ADMIN_JSON_ROOT),
                () -> root, this::commitRoot, List.of()));
    }

    private void buildObject(ConfiguratorGroup group, Supplier<JsonElement> getter,
                             Consumer<JsonElement> setter, List<String> path) {
        var value = getter.get();
        if (value == null || !value.isJsonObject()) return;
        for (var entry : value.getAsJsonObject().entrySet()) {
            String name = entry.getKey();
            List<String> childPath = append(path, name);
            Supplier<JsonElement> childGetter = () -> {
                var current = getter.get();
                return current != null && current.isJsonObject()
                        ? current.getAsJsonObject().get(name) : JsonNull.INSTANCE;
            };
            Consumer<JsonElement> childSetter = next -> {
                var current = getter.get();
                if (current == null || !current.isJsonObject()) return;
                JsonObject copy = current.getAsJsonObject().deepCopy();
                copy.add(name, next.deepCopy());
                setter.accept(copy);
            };
            group.addConfigurator(buildValue(Component.literal(name), childGetter, childSetter, childPath));
        }
    }

    private Configurator buildValue(Component name, Supplier<JsonElement> getter,
                                    Consumer<JsonElement> setter, List<String> path) {
        JsonElement current = getter.get();
        ValueType type = current == null ? ValueType.NULL : ValueType.of(current);
        var result = new ConfiguratorSelectorConfigurator<>("",
                () -> getter.get() == null ? ValueType.NULL : ValueType.of(getter.get()),
                next -> {
                    if (editable() && invalidPaths.isEmpty() && next != null) setter.accept(next.initialValue());
                }, type, true, Arrays.asList(ValueType.values()),
                candidate -> Component.translatable(candidate.translationKey()).getString(),
                (selected, container) -> buildTypedValue(container, getter, setter, path, selected));
        result.setLabel(name);
        inputs.add(new InputControl(result.selector, true));
        return result;
    }

    private void buildTypedValue(ConfiguratorGroup group, Supplier<JsonElement> getter,
                                 Consumer<JsonElement> setter, List<String> path, ValueType type) {
        if (type == ValueType.OBJECT) {
            buildObject(group, getter, setter, path);
        } else if (type == ValueType.ARRAY) {
            var array = new ArrayConfiguratorGroup<JsonItem>(TranslationKeys.GUI_ADMIN_JSON_VALUE, false,
                    () -> arrayItems(getter.get()), (itemGetter, itemSetter) -> {
                var item = itemGetter.get();
                var itemPath = append(path, Integer.toString(item.index()));
                return buildValue(Component.translatable(TranslationKeys.GUI_ADMIN_JSON_VALUE),
                        () -> itemGetter.get().value(),
                        next -> itemSetter.accept(new JsonItem(itemGetter.get().index(), next)), itemPath);
            }, true);
            array.setAddDefault(() -> new JsonItem(arrayItems(getter.get()).size(), JsonNull.INSTANCE));
            array.setOnUpdate(items -> {
                if (!editable()) return;
                var value = new JsonArray();
                items.forEach(item -> value.add(item.value().deepCopy()));
                if (!invalidPaths.isEmpty() && !sameStructure(getter.get(), value)) return;
                setter.accept(value);
            });
            arrays.add(array);
            group.addConfigurator(array);
        } else if (type == ValueType.BOOLEAN) {
            var value = new BooleanConfigurator(TranslationKeys.GUI_ADMIN_JSON_VALUE,
                    () -> primitive(getter.get(), type).getAsBoolean(),
                    next -> update(path, () -> setter.accept(new JsonPrimitive(next))), false, true);
            inputs.add(new InputControl(value.toggle, false));
            group.addConfigurator(value);
        } else if (type == ValueType.STRING) {
            List<String> choices = choices(path);
            if (!choices.isEmpty()) {
                var value = new SelectorConfigurator<>(TranslationKeys.GUI_ADMIN_JSON_VALUE,
                        () -> primitive(getter.get(), type).getAsString(),
                        next -> updateString(path, next, setter),
                        "", true, choices, candidate -> candidate);
                inputs.add(new InputControl(value.selector, false));
                group.addConfigurator(value);
            } else {
                var value = new StringConfigurator(TranslationKeys.GUI_ADMIN_JSON_VALUE,
                        () -> primitive(getter.get(), type).getAsString(),
                        next -> updateString(path, next, setter), "", true);
                value.setTextValidator(text -> validateInput(path, validString(path, text)));
                inputs.add(new InputControl(value.textField, false));
                group.addConfigurator(value);
                var reference = new SelectorConfigurator<>(TranslationKeys.GUI_ADMIN_JSON_REFERENCE,
                        () -> primitive(getter.get(), type).getAsString(),
                        next -> updateString(path, next, setter), "", true, List.of(), candidate -> candidate);
                reference.setDisplay(referenceKind(path) != null);
                references.add(new ReferenceControl(reference, path));
                inputs.add(new InputControl(reference.selector, false));
                group.addConfigurator(reference);
            }
        } else if (type == ValueType.NUMBER) {
            JsonElement current = getter.get();
            String number = primitive(current, type).getAsString();
            try {
                new BigDecimal(number).longValueExact();
                var value = new NumberConfigurator(TranslationKeys.GUI_ADMIN_JSON_VALUE,
                        () -> primitive(getter.get(), type).getAsLong(),
                        next -> update(path, () -> setter.accept(new JsonPrimitive(next))),
                        primitive(current, type).getAsLong(), true).setType(ConfigNumber.Type.LONG);
                inputs.add(new InputControl(value.textField, false));
                value.textField.setTextValidator(text -> validateInput(path, longInput(text)));
                group.addConfigurator(value);
            } catch (ArithmeticException | NumberFormatException failure) {
                var value = new StringConfigurator(TranslationKeys.GUI_ADMIN_JSON_VALUE,
                        () -> primitive(getter.get(), type).getAsString(),
                        next -> update(path, () -> setter.accept(JsonParser.parseString(next))),
                        LDLib2AdminJsonUiConstants.INITIAL_NUMBER, true);
                value.setTextValidator(text -> validateInput(path, text.matches(NUMBER_PATTERN)));
                inputs.add(new InputControl(value.textField, false));
                group.addConfigurator(value);
            }
        } else {
            group.addConfigurator(new Configurator(TranslationKeys.GUI_ADMIN_JSON_NULL));
        }
    }

    private List<JsonItem> arrayItems(@Nullable JsonElement value) {
        if (value == null || !value.isJsonArray()) return List.of();
        List<JsonItem> result = new ArrayList<>();
        for (int index = 0; index < value.getAsJsonArray().size(); index++) {
            result.add(new JsonItem(index, value.getAsJsonArray().get(index)));
        }
        return result;
    }

    private JsonElement primitive(@Nullable JsonElement value, ValueType type) {
        return value != null && ValueType.of(value) == type ? value : type.initialValue();
    }

    private List<String> choices(List<String> path) {
        return observedKey == null ? List.of() : LDLib2AdminJsonFields.forKind(observedKey.kind()).stream()
                .filter(field -> field.path().equals(path)).findFirst()
                .map(LDLib2AdminJsonFields.Field::choices).orElseGet(List::of);
    }

    private @Nullable GachaDefinitionKind referenceKind(List<String> path) {
        return observedKey == null ? null : LDLib2AdminJsonFields.referenceKind(
                observedKey.kind(), path, parentValue(path));
    }

    private boolean validString(List<String> path, String value) {
        GachaDefinitionKind reference = referenceKind(path);
        ResourceLocation id = reference == null ? null : ResourceLocation.tryParse(value);
        List<String> candidates = choices(path);
        return value.length() <= GachaLocalWorkspace.MAXIMUM_DOCUMENT_CHARACTERS
                && (reference == null || !value.isBlank() && id != null && !id.getPath().isEmpty())
                && (candidates.isEmpty() || candidates.contains(value));
    }

    private void updateString(List<String> path, String value, Consumer<JsonElement> setter) {
        if (!editable() || !validateInput(path, validString(path, value))) return;
        update(path, () -> setter.accept(new JsonPrimitive(value)));
    }

    private @Nullable JsonElement parentValue(List<String> path) {
        JsonElement value = root;
        for (String segment : path.subList(0, Math.max(0, path.size() - 1))) {
            if (value == null) return null;
            if (value.isJsonObject()) value = value.getAsJsonObject().get(segment);
            else if (value.isJsonArray()) value = value.getAsJsonArray().get(Integer.parseInt(segment));
            else return null;
        }
        return value;
    }

    private void update(List<String> path, Runnable mutation) {
        if (!editable()) return;
        try {
            mutation.run();
            if (!rejectedMutation) invalidPaths.remove(path);
            updateValidity();
        } catch (RuntimeException failure) {
            invalidPaths.add(List.copyOf(path));
            error = Component.translatable(TranslationKeys.GUI_ADMIN_JSON_INVALID_VALUE);
            updateValidity();
        }
    }

    private void commitRoot(JsonElement value) {
        if (!editable()) return;
        String encoded = JSON.toJson(value);
        try {
            if (!withinLimits(encoded)) {
                error = Component.translatable(TranslationKeys.GUI_ADMIN_JSON_LIMIT,
                        LDLib2AdminJsonUiConstants.MAXIMUM_DEPTH, LDLib2AdminJsonUiConstants.MAXIMUM_NODES);
                rejectedMutation = true;
                invalidPaths.add(List.of());
                updateValidity();
                pendingRebuild = true;
                return;
            }
        } catch (IOException failure) {
            throw new IllegalArgumentException(failure);
        }
        boolean structureChanged = root == null || !sameStructure(root, value);
        root = value;
        observedContent = encoded;
        state.setContent(encoded);
        pendingRebuild |= structureChanged;
    }

    private boolean validateInput(List<String> path, boolean valid) {
        if (valid) invalidPaths.remove(path);
        else invalidPaths.add(List.copyOf(path));
        updateValidity();
        return valid;
    }

    private void updateValidity() {
        state.setInputValid(rawValid && invalidPaths.isEmpty());
        boolean mutable = editable() && invalidPaths.isEmpty();
        inputs.stream().filter(InputControl::structural)
                .forEach(input -> input.element().setActive(mutable));
        arrays.forEach(array -> array.setCanAdd(mutable).setCanRemove(mutable).setCanReorder(mutable));
    }

    private boolean editable() {
        return rawValid && root != null && !pendingRebuild && state.canEditDocument();
    }

    private boolean attached(UIElement element) {
        for (UIElement current = element; current != null; current = current.getParent()) {
            if (current == inspectedGroup) return true;
        }
        return false;
    }

    private boolean sameStructure(JsonElement left, JsonElement right) {
        if (ValueType.of(left) != ValueType.of(right)) return false;
        if (ValueType.of(left) == ValueType.NUMBER && longNumber(left) != longNumber(right)) return false;
        if (left.isJsonObject()) {
            if (!left.getAsJsonObject().keySet().equals(right.getAsJsonObject().keySet())) return false;
            for (String key : left.getAsJsonObject().keySet()) {
                if (!sameStructure(left.getAsJsonObject().get(key), right.getAsJsonObject().get(key))) return false;
            }
        } else if (left.isJsonArray()) {
            if (left.getAsJsonArray().size() != right.getAsJsonArray().size()) return false;
            for (int index = 0; index < left.getAsJsonArray().size(); index++) {
                if (!sameStructure(left.getAsJsonArray().get(index), right.getAsJsonArray().get(index))) return false;
            }
        }
        return true;
    }

    private List<String> append(List<String> path, String segment) {
        List<String> result = new ArrayList<>(path);
        result.add(segment);
        return List.copyOf(result);
    }

    private boolean withinLimits(String content) throws IOException {
        if (content.length() > GachaLocalWorkspace.MAXIMUM_DOCUMENT_CHARACTERS) return false;
        int depth = 0;
        int nodes = 0;
        Deque<Set<String>> objectKeys = new ArrayDeque<>();
        try (var reader = new JsonReader(new StringReader(content))) {
            reader.setLenient(false);
            JsonToken token;
            while ((token = reader.peek()) != JsonToken.END_DOCUMENT) {
                switch (token) {
                    case BEGIN_OBJECT -> {
                        reader.beginObject();
                        objectKeys.push(new HashSet<>());
                        depth++;
                        nodes++;
                    }
                    case BEGIN_ARRAY -> {
                        reader.beginArray();
                        objectKeys.push(Set.of());
                        depth++;
                        nodes++;
                    }
                    case END_OBJECT -> {
                        reader.endObject();
                        objectKeys.pop();
                        depth--;
                    }
                    case END_ARRAY -> {
                        reader.endArray();
                        objectKeys.pop();
                        depth--;
                    }
                    case NAME -> {
                        Set<String> keys = objectKeys.peek();
                        if (keys == null || !keys.add(reader.nextName())) {
                            throw new IllegalStateException(Component.translatable(
                                    TranslationKeys.GUI_ADMIN_JSON_DUPLICATE_KEY).getString());
                        }
                    }
                    case STRING, NUMBER -> {
                        reader.nextString();
                        nodes++;
                    }
                    case BOOLEAN -> {
                        reader.nextBoolean();
                        nodes++;
                    }
                    case NULL -> {
                        reader.nextNull();
                        nodes++;
                    }
                    default -> throw new IllegalStateException();
                }
                if (depth > LDLib2AdminJsonUiConstants.MAXIMUM_DEPTH
                        || nodes > LDLib2AdminJsonUiConstants.MAXIMUM_NODES) return false;
            }
        }
        return nodes > 0 && depth == 0;
    }

    private boolean longInput(String text) {
        try {
            Long.parseLong(text);
            return true;
        } catch (NumberFormatException failure) {
            return false;
        }
    }

    private boolean longNumber(JsonElement value) {
        try {
            new BigDecimal(value.getAsString()).longValueExact();
            return true;
        } catch (ArithmeticException | NumberFormatException failure) {
            return false;
        }
    }

    private record InputControl(UIElement element, boolean structural) {
    }

    private record ReferenceControl(SelectorConfigurator<String> configurator, List<String> path) {
    }

    private record JsonItem(int index, JsonElement value) {
    }
}
