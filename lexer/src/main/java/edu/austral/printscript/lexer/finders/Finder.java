package edu.austral.printscript.lexer.finders;

import edu.austral.printscript.common.token.Token;

public interface Finder {
    boolean canHandle(char currentChar);
    Token find(String input, int startIndex, int row, int column);
}