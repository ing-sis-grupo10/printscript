package printscript.lexer.finders;

import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

import java.util.Map;

public class SymbolFinder implements Finder {

    private static final Map<Character, TokenType> SYMBOLS = Map.ofEntries(
            Map.entry(':', TokenType.COLON),
            Map.entry(';', TokenType.SEMICOLON),
            Map.entry('=', TokenType.ASSIGN),
            Map.entry('+', TokenType.PLUS),
            Map.entry('-', TokenType.MINUS),
            Map.entry('*', TokenType.STAR),
            Map.entry('/', TokenType.SLASH),
            Map.entry('(', TokenType.LEFT_PAREN),
            Map.entry(')', TokenType.RIGHT_PAREN)
    );

    @Override
    public boolean canHandle(char currentChar) {
        return SYMBOLS.containsKey(currentChar);
    }

    @Override
    public Token find(String input, int startIndex, int row, int column) {
        char symbol = input.charAt(startIndex);
        TokenType type = SYMBOLS.get(symbol);
        Span span = Span.of(new Position(row, column), new Position(row, column + 1));
        return new Token(type, String.valueOf(symbol), span);
    }
}
