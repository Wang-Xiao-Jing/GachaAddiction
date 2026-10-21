package xiaojin.gachaaddiction.config.entry;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class FilterListConfigEntry extends BasicConfigEntry<List<List<String>>> {
    private final Predicate<Object> conditionValidator;

    public FilterListConfigEntry(ModConfigSpec.ConfigValue<List<?>> rawValue, List<String> path,
                                 String translationKey, List<String> comments, List<List<String>> defaultValue,
                                 Predicate<Object> conditionValidator) {
        super(rawValue, path, translationKey, comments, copyRules(defaultValue),
                rules -> areRulesValid(rules, conditionValidator),
                () -> decode(rawValue.get(), conditionValidator),
                rules -> rawValue.set(encode(rules)));
        this.conditionValidator = Objects.requireNonNull(conditionValidator);
    }

    public static Builder builder(ModConfigSpec.Builder specBuilder, String modId, List<String> currentPath) {
        return new Builder(specBuilder, modId, currentPath);
    }

    private static List<List<String>> decode(List<?> values, Predicate<Object> conditionValidator) {
        List<List<String>> rules = new ArrayList<>();
        for (Object value : values) {
            List<String> rule = List.of();
            if (value instanceof String condition) {
                rule = splitRule(condition).stream()
                        .filter(entry -> isConditionValid(entry, conditionValidator))
                        .toList();
            } else if (value instanceof List<?> storedRule) {
                rule = storedRule.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .filter(entry -> isConditionValid(entry, conditionValidator))
                        .toList();
            }
            if (!rule.isEmpty()) {
                rules.add(rule);
            }
        }
        return List.copyOf(rules);
    }

    private static List<Object> encode(List<List<String>> rules) {
        return rules.stream().map(FilterListConfigEntry::encodeRule).toList();
    }

    private static Object encodeRule(List<String> rule) {
        return rule.size() == 1 ? rule.getFirst() : List.copyOf(rule);
    }

    private static List<List<String>> copyRules(List<? extends List<String>> rules) {
        return rules.stream().map(List::copyOf).toList();
    }

    private static boolean areRulesValid(List<List<String>> rules, Predicate<Object> conditionValidator) {
        return rules != null && rules.stream().allMatch(rule -> isRuleValid(rule, conditionValidator));
    }

    private static boolean isRuleValid(List<String> rule, Predicate<Object> conditionValidator) {
        return rule != null && !rule.isEmpty()
                && rule.stream().allMatch(condition -> isConditionValid(condition, conditionValidator));
    }

    private static boolean isConditionValid(Object condition, Predicate<Object> conditionValidator) {
        try {
            return conditionValidator.test(condition);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isStoredRuleValid(Object value, Predicate<Object> conditionValidator) {
        if (value instanceof String condition) {
            return isRuleValid(splitRule(condition), conditionValidator);
        }
        if (!(value instanceof List<?> rule) || rule.isEmpty()) {
            return false;
        }
        return rule.stream().allMatch(condition -> condition instanceof String
                && isConditionValid(condition, conditionValidator));
    }

    private static List<String> splitRule(String value) {
        List<String> conditions = new ArrayList<>();
        List<Character> delimiters = new ArrayList<>();
        char quote = 0;
        boolean escaped = false;
        int conditionStart = 0;
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (quote != 0) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == quote) {
                    quote = 0;
                }
                continue;
            }
            if (character == '\'' || character == '"') {
                quote = character;
            } else if (character == '[' || character == '{' || character == '(') {
                delimiters.add(character);
            } else if (character == ']' || character == '}' || character == ')') {
                if (!delimiters.isEmpty()) {
                    delimiters.removeLast();
                }
            } else if (character == ';' && delimiters.isEmpty()) {
                conditions.add(value.substring(conditionStart, i).strip());
                conditionStart = i + 1;
            }
        }
        conditions.add(value.substring(conditionStart).strip());
        return List.copyOf(conditions);
    }

    public boolean isConditionValid(String condition) {
        try {
            return conditionValidator.test(condition);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public boolean isRuleValid(List<String> rule) {
        return isRuleValid(rule, conditionValidator);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder extends BasicConfigEntry.Builder<FilterListConfigEntry, Builder> {
        private List<List<String>> defaultValue = List.of();
        private Predicate<Object> conditionValidator;

        private Builder(ModConfigSpec.Builder specBuilder, String modId, List<String> currentPath) {
            super(specBuilder, modId, currentPath);
        }

        public Builder defaultValue(List<? extends List<String>> defaultValue) {
            this.defaultValue = copyRules(defaultValue);
            return this;
        }

        public Builder conditionValidator(Predicate<Object> conditionValidator) {
            this.conditionValidator = Objects.requireNonNull(conditionValidator);
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FilterListConfigEntry build() {
            if (conditionValidator == null || !areRulesValid(defaultValue, conditionValidator)) {
                throw new IllegalStateException("Default rules and condition validator must be valid");
            }
            List<Object> encodedDefaultValue = encode(defaultValue);
            var value = prepareDefinition().defineList(key, encodedDefaultValue, () -> "",
                    element -> isStoredRuleValid(element, conditionValidator));
            return new FilterListConfigEntry(value, path(), translationKey(), comments, defaultValue, conditionValidator);
        }
    }
}
