package printscript.lexer;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import printscript.common.Version;
import printscript.common.token.TokenType;
import printscript.lexer.finders.Finder;
import printscript.lexer.finders.IdentifierFinder;
import printscript.lexer.finders.KeywordFinder;
import printscript.lexer.finders.NumberFinder;
import printscript.lexer.finders.StringFinder;
import printscript.lexer.finders.SymbolFinder;

// Único lugar que sabe qué reconoce el lexer en cada versión del lenguaje.
public final class LexerFactory {

    private static final Map<String, TokenType> V1_0_KEYWORDS =
            Map.of(
                    "let", TokenType.LET,
                    "number", TokenType.NUMBER_TYPE,
                    "string", TokenType.STRING_TYPE,
                    "println", TokenType.PRINTLN);

    private static final Map<String, TokenType> V1_1_KEYWORDS =
            withAll(
                    V1_0_KEYWORDS,
                    Map.of(
                            "boolean", TokenType.BOOLEAN_TYPE,
                            "const", TokenType.CONST,
                            "if", TokenType.IF,
                            "else", TokenType.ELSE,
                            "true", TokenType.TRUE,
                            "false", TokenType.FALSE,
                            "readInput", TokenType.READ_INPUT,
                            "readEnv", TokenType.READ_ENV));

    private LexerFactory() {}

    public static PrintScriptLexer create(Reader reader, Version version) {
        return new PrintScriptLexer(reader, findersFor(version));
    }

    public static List<Finder> findersFor(Version version) {
        return List.of(
                new KeywordFinder(keywordsFor(version)),
                new IdentifierFinder(),
                new NumberFinder(),
                new StringFinder(),
                new SymbolFinder());
    }

    private static Map<String, TokenType> keywordsFor(Version version) {
        return switch (version) {
            case V1_0 -> V1_0_KEYWORDS;
            case V1_1 -> V1_1_KEYWORDS;
        };
    }

    private static Map<String, TokenType> withAll(
            Map<String, TokenType> base, Map<String, TokenType> extra) {
        Map<String, TokenType> merged = new HashMap<>(base);
        merged.putAll(extra);
        return Map.copyOf(merged);
    }
}
