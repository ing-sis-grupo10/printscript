package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

// Alrededor de los paréntesis se respeta el original, salvo single_space_separation.
public record ParenthesisSpacingRule(boolean singleSpaceSeparation) implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        boolean touchesParen =
                current.type() == TokenType.LEFT_PAREN
                        || previous.type() == TokenType.LEFT_PAREN
                        || current.type() == TokenType.RIGHT_PAREN;
        if (!touchesParen) {
            return Optional.empty();
        }
        return Optional.of(singleSpaceSeparation ? " " : context.originalGap());
    }
}
