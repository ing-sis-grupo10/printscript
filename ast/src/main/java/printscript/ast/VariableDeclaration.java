package printscript.ast;

import printscript.diagnostics.Span;

import java.util.Optional;

public record VariableDeclaration(
        String name,
        DeclaredType declaredType,
        Optional<Expression> initializer,
        Span span
) implements Statement {}