package edu.austral.printscript.lexer.finders;

import edu.austral.printscript.common.token.Position;
import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;
import edu.austral.printscript.lexer.patterns.LetterPattern;
import edu.austral.printscript.lexer.patterns.LetterOrDigitPattern;
import edu.austral.printscript.lexer.patterns.Pattern;


public class IdentifierFinder implements Finder {

    private final Pattern letterPattern = new LetterPattern();
    private final Pattern letterOrDigitPattern = new LetterOrDigitPattern();

    @Override
    public boolean canHandle(char currentChar) {
        return letterPattern.matches(currentChar);
    }

    @Override
    public Token find(String input, int startIndex, int row, int column) {
        StringBuilder lexeme = new StringBuilder();
        int index = startIndex;
        int startColumn = column;

        while (index < input.length() && letterOrDigitPattern.matches(input.charAt(index))) {
            lexeme.append(input.charAt(index));
            index++;
            column++;
        }

        Position position = new Position(row, startColumn, row, column);
        return new Token(TokenType.IDENTIFIER, lexeme.toString(), position);
    }
}