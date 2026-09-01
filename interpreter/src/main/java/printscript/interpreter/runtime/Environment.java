package printscript.interpreter.runtime;

import java.util.Optional;
import printscript.ast.DeclaredType;
import printscript.common.result.Diagnostic;
import printscript.common.token.Span;

public interface Environment {
    Optional<Diagnostic> declare(String name, DeclaredType type, Span declarationSite);

    void assign(String name, RuntimeValue value);

    Optional<DeclaredType> typeOf(String name);

    Optional<RuntimeValue> valueOf(String name);
}
