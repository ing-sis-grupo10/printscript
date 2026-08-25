package printscript.lexer.finders;

import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.lexer.patterns.Pattern;

// Compartido por IdentifierFinder y KeywordFinder: ambos escanean
// mientras el carácter matchee un Pattern (letra/dígito) y difieren
// solo en qué Token arman con el resultado.
abstract class AbstractPatternFinder implements Finder {

    protected record Scan(String value, Span span) {}

    protected Scan consumeWhile(String input, int startIndex, int row, int column, Pattern pattern) {
        StringBuilder value = new StringBuilder();
        int index = startIndex;
        int startColumn = column;

        while (index < input.length() && pattern.matches(input.charAt(index))) {
            value.append(input.charAt(index));
            index++;
            column++;
        }

        Span span = Span.of(new Position(row, startColumn), new Position(row, column));
        return new Scan(value.toString(), span);
    }
}
