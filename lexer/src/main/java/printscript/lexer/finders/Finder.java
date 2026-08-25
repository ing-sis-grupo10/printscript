package printscript.lexer.finders;

import java.util.Optional;
import printscript.common.result.Result;
import printscript.common.token.Token;

public interface Finder {
    boolean canHandle(char currentChar);

    Optional<Result<Token>> find(String input, int startIndex, int row, int column);
}
