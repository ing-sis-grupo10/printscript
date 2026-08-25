package printscript.ast;

import java.util.Optional;
import printscript.common.token.Span;

public record VariableDeclaration(
        String name, DeclaredType declaredType, Optional<Expression> initializer, Span span)
        implements Statement {}
