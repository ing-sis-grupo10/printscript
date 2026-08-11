package printscript.ast;

import edu.austral.printscript.common.token.Span;

import java.util.Optional;

public record VariableDeclaration(
        String name,
        DeclaredType declaredType,
        Optional<Expression> initializer,
        Span span
) implements Statement {}