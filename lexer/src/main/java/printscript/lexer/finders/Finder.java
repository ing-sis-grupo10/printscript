package printscript.lexer.finders;

import printscript.common.result.Result;
import printscript.common.token.Token;

import java.util.Optional;

public interface Finder {
    boolean canHandle(char currentChar);
    Optional<Result<Token>> find(String input, int startIndex, int row, int column);
}