package edu.austral.printscript.lexer.finders;

import edu.austral.printscript.common.exception.InvalidTokenException;
import edu.austral.printscript.common.token.Position;
import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;

public class StringFinder implements Finder {

    private static final char QUOTE = '"';

    @Override
    public boolean canHandle(char currentChar) {
        return currentChar == QUOTE;
    }

    @Override
    public Token find(String input, int startIndex, int row, int column) {
        StringBuilder lexeme = new StringBuilder();
        int index = startIndex + 1;
        int startColumn = column;
        column++;

        while (index < input.length() && input.charAt(index) != QUOTE) {
            lexeme.append(input.charAt(index));
            index++;
            column++;
        }

        if (index >= input.length()) {
            Position errorPosition = new Position(row, startColumn, row, column);
            throw new InvalidTokenException("String sin cerrar", errorPosition);
        }

        column++;
        Position position = new Position(row, startColumn, row, column);
        return new Token(TokenType.STRING_LITERAL, lexeme.toString(), position);
    }
}