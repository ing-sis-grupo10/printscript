package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

// Saltos de línea al entrar y salir de un bloque; "} else" queda en la misma línea.
public final class BlockLineBreakRule implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        if (previous.type() == TokenType.LEFT_BRACE || current.type() == TokenType.RIGHT_BRACE) {
            return Optional.of(context.lineBreak(1));
        }
        if (previous.type() == TokenType.RIGHT_BRACE) {
            return Optional.of(current.type() == TokenType.ELSE ? " " : context.lineBreak(1));
        }
        return Optional.empty();
    }
}
