package printscript.lexer.finders;

import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.patterns.LetterPattern;
import printscript.lexer.patterns.LetterOrDigitPattern;
import printscript.lexer.patterns.Pattern;


public class IdentifierFinder extends AbstractPatternFinder {

    private final Pattern letterPattern = new LetterPattern();
    private final Pattern letterOrDigitPattern = new LetterOrDigitPattern();

    @Override
    public boolean canHandle(char currentChar) {
        return letterPattern.matches(currentChar);
    }


    @Override
    public Token find(String input, int startIndex, int row, int column) {
        Scan scan = consumeWhile(input, startIndex, row, column, letterOrDigitPattern);
        return new Token(TokenType.IDENTIFIER, scan.value(), scan.span());
    }
}
