package printscript.formatter;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.formatter.spacing.FormatContext;

// Lleva el estado que se acumula mientras se recorren los tokens (indentación, si la
// sentencia anterior fue un println, cuál fue el token anterior) y arma el FormatContext
// que reciben las reglas. El formatter solo escribe; acá vive todo el seguimiento.
final class FormatState {
    private final int indentSize;
    private Token previous;
    private int depth;
    private boolean sawPrintlnInCurrentStatement;
    private boolean previousStatementWasPrintln;

    FormatState(int indentSize) {
        this.indentSize = indentSize;
    }

    // "}" baja la indentación antes de escribirse: se alinea con el bloque de afuera.
    void beforeToken(Token token) {
        if (token.type() == TokenType.RIGHT_BRACE) {
            depth--;
        }
    }

    void afterToken(Token token) {
        switch (token.type()) {
            case LEFT_BRACE -> depth++;
            case PRINTLN -> sawPrintlnInCurrentStatement = true;
            case SEMICOLON -> {
                previousStatementWasPrintln = sawPrintlnInCurrentStatement;
                sawPrintlnInCurrentStatement = false;
            }
            default -> {}
        }
        previous = token;
    }

    Optional<Token> previous() {
        return Optional.ofNullable(previous);
    }

    FormatContext context(String originalGap) {
        return new FormatContext(originalGap, depth, indentSize, previousStatementWasPrintln);
    }
}
