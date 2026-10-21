package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.CodeEditor;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.language.LanguageDefinition;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.language.StyleManager;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.language.TokenType;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.language.TokenTypes;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.Set;

public class LDLib2AdminJsonCodeEditor extends CodeEditor {
    private static final String LANGUAGE_NAME = "JSON";
    private static final String STRING_PATTERN = "\"(?:\\\\.|[^\"\\\\])*\"";
    private static final TokenType PROPERTY = new TokenType("JsonProperty")
            .setPattern(STRING_PATTERN + "(?=\\s*:)");
    private static final TokenType STRING = new TokenType("JsonString").setPattern(STRING_PATTERN);
    private static final TokenType NUMBER = new TokenType("JsonNumber")
            .setPattern("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
    private static final TokenType KEYWORD = TokenTypes.KEYWORD.createTokenType(List.of("true", "false", "null"));
    private static final TokenType PUNCTUATION = new TokenType("JsonPunctuation").setPattern("[{}\\[\\],:]");
    private static final LanguageDefinition JSON_LANGUAGE = new LanguageDefinition(LANGUAGE_NAME,
            List.of(PROPERTY, STRING, NUMBER, KEYWORD, PUNCTUATION, TokenTypes.WHITESPACE, TokenTypes.OTHER),
            Set.of());

    public LDLib2AdminJsonCodeEditor() {
        var styles = new StyleManager();
        styles.getStyleMap().put(PROPERTY.name, Style.EMPTY.withColor(ColorPattern.ORANGE.color));
        styles.getStyleMap().put(STRING.name, Style.EMPTY.withColor(ColorPattern.GREEN.color));
        styles.getStyleMap().put(NUMBER.name, Style.EMPTY.withColor(ColorPattern.CYAN.color));
        styles.getStyleMap().put(PUNCTUATION.name, Style.EMPTY.withColor(ColorPattern.WHITE.color));
        setLanguage(JSON_LANGUAGE);
        setStyleManager(styles);
    }

    @Override
    protected void toggleCommentAtBol() {
    }
}
