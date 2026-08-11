package edu.austral.printscript.lexer.finders;

import edu.austral.printscript.common.token.Position;
import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;
import edu.austral.printscript.lexer.patterns.DigitPattern;
import edu.austral.printscript.lexer.patterns.Pattern;

public class NumberFinder implements Finder {

    private final Pattern digitPattern = new DigitPattern();

    @Override
    public boolean canHandle(char currentChar) {
        return digitPattern.matches(currentChar);
    }

    @Override
    public Token find(String input, int startIndex, int row, int column) {
        StringBuilder lexeme = new StringBuilder();
        int index = startIndex;
        int startColumn = column;

        while (index < input.length() && digitPattern.matches(input.charAt(index))) {
            lexeme.append(input.charAt(index));
            index++;
            column++;
        }

        Position position = new Position(row, startColumn, row, column);
        return new Token(TokenType.NUMBER_LITERAL, lexeme.toString(), position);
    }
}