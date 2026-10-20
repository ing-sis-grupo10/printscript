package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

// "{" en la misma línea del if o en la siguiente.
public record IfBracePlacementRule(boolean braceSameLine) implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        if (current.type() != TokenType.LEFT_BRACE) {
            return Optional.empty();
        }
        return Optional.of(braceSameLine ? " " : context.lineBreak(1));
    }
}
