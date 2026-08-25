package printscript.lexer.finders;

import java.util.Optional;
import printscript.common.result.Diagnostic;
import printscript.common.result.Result;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

public class StringFinder implements Finder {

    @Override
    public boolean canHandle(char currentChar) {
        return currentChar == '"' || currentChar == '\'';
    }

    @Override
    public Optional<Result<Token>> find(String input, int startIndex, int row, int column) {
        char quote = input.charAt(startIndex);
        StringBuilder value = new StringBuilder();
        int index = startIndex + 1;
        int startColumn = column;
        column++;

        while (index < input.length() && input.charAt(index) != quote) {
            value.append(input.charAt(index));
            index++;
            column++;
        }

        if (index >= input.length()) {
            Span errorSpan = Span.of(new Position(row, startColumn), new Position(row, column));
            return Optional.of(Result.failure(Diagnostic.error("String sin cerrar", errorSpan)));
        }

        column++;
        Span span = Span.of(new Position(row, startColumn), new Position(row, column));
        return Optional.of(
                Result.success(new Token(TokenType.STRING_LITERAL, value.toString(), span)));
    }
}
