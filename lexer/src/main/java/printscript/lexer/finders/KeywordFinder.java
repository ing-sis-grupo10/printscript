package printscript.lexer.finders;

import java.util.Map;
import java.util.Optional;
import printscript.common.result.Result;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.patterns.LetterOrDigitPattern;
import printscript.lexer.patterns.Pattern;

// No sabe de versiones: recibe la tabla de keywords que le corresponda.
// Qué keywords trae cada versión lo decide LexerFactory.
public class KeywordFinder extends AbstractPatternFinder {

    private final Map<String, TokenType> keywords;
    private final Pattern letterOrDigitPattern = new LetterOrDigitPattern();

    public KeywordFinder(Map<String, TokenType> keywords) {
        this.keywords = Map.copyOf(keywords);
    }

    @Override
    public boolean canHandle(char currentChar) {
        return Character.isLetter(currentChar);
    }

    @Override
    public Optional<Result<Token>> find(String input, int startIndex, int row, int column) {
        Scan scan = consumeWhile(input, startIndex, row, column, letterOrDigitPattern);
        TokenType type = keywords.get(scan.value());
        if (type == null) {
            return Optional.empty();
        }
        return Optional.of(Result.success(new Token(type, scan.value(), scan.span())));
    }
}
