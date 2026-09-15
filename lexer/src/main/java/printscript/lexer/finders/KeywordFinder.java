package printscript.lexer.finders;

import java.util.Map;
import java.util.Optional;
import printscript.common.result.Result;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.patterns.LetterOrDigitPattern;
import printscript.lexer.patterns.Pattern;

public class KeywordFinder extends AbstractPatternFinder {

    private static final Map<String, TokenType> KEYWORDS =
            Map.ofEntries(
                    Map.entry("let", TokenType.LET),
                    Map.entry("number", TokenType.NUMBER_TYPE),
                    Map.entry("string", TokenType.STRING_TYPE),
                    Map.entry("boolean", TokenType.BOOLEAN_TYPE),
                    Map.entry("println", TokenType.PRINTLN),
                    Map.entry("const", TokenType.CONST),
                    Map.entry("if", TokenType.IF),
                    Map.entry("else", TokenType.ELSE),
                    Map.entry("true", TokenType.TRUE),
                    Map.entry("false", TokenType.FALSE),
                    Map.entry("readInput", TokenType.READ_INPUT),
                    Map.entry("readEnv", TokenType.READ_ENV));

    private final Pattern letterOrDigitPattern = new LetterOrDigitPattern();

    @Override
    public boolean canHandle(char currentChar) {
        return Character.isLetter(currentChar);
    }

    @Override
    public Optional<Result<Token>> find(String input, int startIndex, int row, int column) {
        Scan scan = consumeWhile(input, startIndex, row, column, letterOrDigitPattern);
        TokenType type = KEYWORDS.get(scan.value());
        if (type == null) {
            return Optional.empty();
        }
        return Optional.of(Result.success(new Token(type, scan.value(), scan.span())));
    }
}
