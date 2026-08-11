package edu.austral.printscript.lexer.finders;

import edu.austral.printscript.common.token.Position;
import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;
import edu.austral.printscript.lexer.patterns.LetterOrDigitPattern;
import edu.austral.printscript.lexer.patterns.Pattern;

import java.util.Map;

public class KeywordFinder implements Finder {

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
        StringBuilder lexeme = new StringBuilder();
        int index = startIndex;
        int startColumn = column;

        while (index < input.length() && letterOrDigitPattern.matches(input.charAt(index))) {
            lexeme.append(input.charAt(index));
            index++;
            column++;
        }

        String word = lexeme.toString();
        TokenType type = KEYWORDS.get(word);

        if (type == null) {
            return null;
        }

        Position position = new Position(row, startColumn, row, column);
        return new Token(type, word, position);
    }
}