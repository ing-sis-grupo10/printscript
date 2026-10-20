package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

// "if (" siempre con un espacio.
public final class IfKeywordSpacingRule implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        boolean ifParen = previous.type() == TokenType.IF && current.type() == TokenType.LEFT_PAREN;
        return ifParen ? Optional.of(" ") : Optional.empty();
    }
}
