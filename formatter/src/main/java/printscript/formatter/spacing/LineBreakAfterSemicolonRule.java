package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

// Salto de línea obligatorio después de ";" (no se configura), más las líneas en blanco
// configurables cuando la sentencia anterior fue un println.
public record LineBreakAfterSemicolonRule(int blankLinesAfterPrintln) implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        if (previous.type() != TokenType.SEMICOLON) {
            return Optional.empty();
        }
        int blankLines = context.previousStatementWasPrintln() ? blankLinesAfterPrintln : 0;
        return Optional.of(context.lineBreak(1 + blankLines));
    }
}
