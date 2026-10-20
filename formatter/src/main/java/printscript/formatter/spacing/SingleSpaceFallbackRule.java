package printscript.formatter.spacing;

import java.util.Optional;
import printscript.common.token.Token;

// Última de la cadena: si ninguna otra decidió, un solo espacio
// ("debe haber un solo espacio como máximo entre tokens", por consigna).
public final class SingleSpaceFallbackRule implements SpacingRule {

    @Override
    public Optional<String> separator(Token previous, Token current, FormatContext context) {
        return Optional.of(" ");
    }
}
