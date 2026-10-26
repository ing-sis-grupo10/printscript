package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;

// Decide qué va entre dos tokens consecutivos.
// Optional.empty() = "este par no me corresponde", y decide la siguiente regla de la lista.
@FunctionalInterface
public interface SpacingRule {
    Optional<String> separator(Token previous, Token current, FormatContext context);
}
