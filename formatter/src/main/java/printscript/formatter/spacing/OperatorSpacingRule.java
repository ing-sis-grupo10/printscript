package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

// Un espacio antes y después de cada operador aritmético (por consigna, no se configura).
public final class OperatorSpacingRule implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        if (isArithmeticOperator(previous.type()) || isArithmeticOperator(current.type())) {
            return Optional.of(" ");
        }
        return Optional.empty();
    }

    static boolean isArithmeticOperator(TokenType type) {
        return type == TokenType.PLUS
                || type == TokenType.MINUS
                || type == TokenType.STAR
                || type == TokenType.SLASH;
    }
}
