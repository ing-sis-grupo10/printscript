package printscript.lexer.finders;

import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.patterns.LetterOrDigitPattern;
import printscript.lexer.patterns.Pattern;

import java.util.Map;

public class KeywordFinder extends AbstractPatternFinder {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "let", TokenType.LET,
            "number", TokenType.NUMBER_TYPE,
            "string", TokenType.STRING_TYPE,
            "println", TokenType.PRINTLN
    );

    private final Pattern letterOrDigitPattern = new LetterOrDigitPattern();

    @Override
    public boolean canHandle(char currentChar) {
        return Character.isLetter(currentChar);
    }

    @Override
    public Token find(String input, int startIndex, int row, int column) {
        Scan scan = consumeWhile(input, startIndex, row, column, letterOrDigitPattern);
        TokenType type = KEYWORDS.get(scan.value());
        return type == null ? null : new Token(type, scan.value(), scan.span());
    }
}

