package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

// Espacio antes/después de un token (":" en declaraciones, "=" en asignaciones).
// Si la regla está configurada se fuerza; si no, se respeta el espaciado original
// (o un espacio, si está activado single_space_separation).
public record AroundTokenSpacingRule(
        TokenType token,
        Optional<Boolean> spaceBefore,
        Optional<Boolean> spaceAfter,
        boolean singleSpaceSeparation)
        implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        if (current.type() == token) {
            return Optional.of(resolve(spaceBefore, context.originalGap()));
        }
        if (previous.type() == token) {
            return Optional.of(resolve(spaceAfter, context.originalGap()));
        }
        return Optional.empty();
    }

    private String resolve(Optional<Boolean> configured, String originalGap) {
        if (configured.isPresent()) {
            return configured.get() ? " " : "";
        }
        return singleSpaceSeparation ? " " : originalGap;
    }
}
