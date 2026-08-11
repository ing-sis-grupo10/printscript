package edu.austral.printscript.lexer.finders;

import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;
import edu.austral.printscript.lexer.patterns.LetterPattern;
import edu.austral.printscript.lexer.patterns.LetterOrDigitPattern;
import edu.austral.printscript.lexer.patterns.Pattern;


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