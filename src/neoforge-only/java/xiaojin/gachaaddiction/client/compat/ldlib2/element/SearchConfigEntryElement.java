package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.editor.resource.IResourcePath;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2Templates;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class SearchConfigEntryElement extends BasicConfigEntryElement<String> {
    private static final char NEGATION_PREFIX = '!';
    private static final char TAG_PREFIX = '#';
    private static volatile CandidateCache candidateCache;

    private final SearchComponent<String> valueElement;
    private final List<String> candidates;
    private final Set<String> candidateSet;
    private volatile String searchText = "";

    public SearchConfigEntryElement() {
        this(LDLib2Templates.SEARCH_CONFIG_ENTRY);
    }

    @SuppressWarnings("unchecked")
    public SearchConfigEntryElement(@Nullable IResourcePath templatePath) {
        super(templatePath);
        valueElement = select("#value", SearchComponent.class).findFirst().orElseThrow();
        CandidateCache cache = candidates();
        candidates = cache.values();
        candidateSet = cache.valueSet();
        valueElement.setSearchUI(new SearchComponent.ISearchUI<>() {
            @Override
            public @NotNull String resultText(@NotNull String value) {
                return candidateSet.contains(value) ? replaceSubject(searchText, value) : value;
            }

            @Override
            public void onResultSelected(@Nullable String value) {
                if (value != null) {
                    valueElement.setValue(valueElement.textField.getValue(), false);
                }
            }

            @Override
            public void search(String word, IResultHandler<String> searchHandler) {
                searchText = word;
                String subject = subjectOf(word).toLowerCase(Locale.ROOT);
                boolean tagsOnly = subject.startsWith(Character.toString(TAG_PREFIX));
                for (String candidate : candidates) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    if ((!tagsOnly || candidate.startsWith(Character.toString(TAG_PREFIX)))
                            && candidate.toLowerCase(Locale.ROOT).contains(subject)) {
                        searchHandler.accept(candidate);
                    }
                }
            }
        });
        getTextField().setTextResponder(text -> {
            searchText = text;
            if (!Objects.equals(valueElement.getValue(), text)) {
                valueElement.setValue(text, false);
            }
        });
    }

    public SearchComponent<String> getValueElement() {
        return valueElement;
    }

    public TextField getTextField() {
        return valueElement.textField;
    }

    @SuppressWarnings("UnusedReturnValue")
    public SearchConfigEntryElement setTextValidator(Predicate<String> textValidator) {
        getTextField().setTextValidator(textValidator);
        return this;
    }

    @Override
    public String getValue() {
        return getTextField().getValue();
    }

    @Override
    public SearchConfigEntryElement setText(String text) {
        return (SearchConfigEntryElement) super.setText(text);
    }

    @Override
    public SearchConfigEntryElement setText(Component component) {
        return (SearchConfigEntryElement) super.setText(component);
    }

    @Override
    public SearchConfigEntryElement setValue(String value) {
        searchText = value;
        valueElement.setValue(value, false);
        return this;
    }

    private static List<String> createCandidates() {
        var values = new LinkedHashSet<String>();
        BuiltInRegistries.ITEM.keySet().forEach(id -> values.add(id.toString()));
        BuiltInRegistries.ITEM.getTagNames().forEach(tag -> values.add(TAG_PREFIX + tag.location().toString()));
        return List.copyOf(values);
    }

    public static void invalidateCandidates() {
        candidateCache = null;
    }

    private static CandidateCache candidates() {
        CandidateCache cache = candidateCache;
        if (cache != null) {
            return cache;
        }
        synchronized (SearchConfigEntryElement.class) {
            cache = candidateCache;
            if (cache == null) {
                List<String> values = createCandidates();
                cache = new CandidateCache(values, Set.copyOf(values));
                candidateCache = cache;
            }
            return cache;
        }
    }

    private static String subjectOf(String value) {
        String stripped = value.stripLeading();
        int subjectStart = stripped.startsWith(Character.toString(NEGATION_PREFIX)) ? 1 : 0;
        int subjectEnd = findSubjectEnd(stripped, subjectStart);
        return stripped.substring(subjectStart, subjectEnd);
    }

    private static String replaceSubject(String value, String replacement) {
        int leadingLength = value.length() - value.stripLeading().length();
        String stripped = value.substring(leadingLength);
        boolean negated = stripped.startsWith(Character.toString(NEGATION_PREFIX));
        int subjectStart = negated ? 1 : 0;
        int subjectEnd = findSubjectEnd(stripped, subjectStart);
        return value.substring(0, leadingLength)
                + (negated ? Character.toString(NEGATION_PREFIX) : "")
                + replacement
                + stripped.substring(subjectEnd);
    }

    private static int findSubjectEnd(String value, int subjectStart) {
        for (int i = subjectStart; i < value.length(); i++) {
            char character = value.charAt(i);
            if (isSubjectSuffix(character) || Character.isWhitespace(character)) {
                return i;
            }
        }
        return value.length();
    }

    private static boolean isSubjectSuffix(char character) {
        return switch (character) {
            case '[', '{', ';' -> true;
            default -> false;
        };
    }

    private record CandidateCache(List<String> values, Set<String> valueSet) {
    }
}
