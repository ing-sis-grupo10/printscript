package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

public final class NoSpaceBeforeSemicolonRule implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        return current.type() == TokenType.SEMICOLON ? Optional.of("") : Optional.empty();
    }
}
