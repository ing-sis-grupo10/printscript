package printscript.lexer.finders;

import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.patterns.DigitPattern;
import printscript.lexer.patterns.Pattern;

public class NumberFinder implements Finder {

    private final Pattern digitPattern = new DigitPattern();

    @Override
    public boolean canHandle(char currentChar) {
        return digitPattern.matches(currentChar);
    }

    @Override
    public Token find(String input, int startIndex, int row, int column) {
        StringBuilder value = new StringBuilder();
        int index = startIndex;
        int startColumn = column;
        boolean seenDot = false;

        while (index < input.length()) {
            char c = input.charAt(index);
            if (digitPattern.matches(c)) {
                value.append(c);
                index++;
                column++;
            } else if (c == '.' && !seenDot && index + 1 < input.length() && digitPattern.matches(input.charAt(index + 1))) {
                seenDot = true;
                value.append(c);
                index++;
                column++;
            } else {
                break;
            }
        }

        Span span = Span.of(new Position(row, startColumn), new Position(row, column));
        return new Token(TokenType.NUMBER_LITERAL, value.toString(), span);
    }
}
