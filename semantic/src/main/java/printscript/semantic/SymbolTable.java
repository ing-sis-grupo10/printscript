package printscript.semantic;

import java.util.Optional;
import printscript.common.result.Diagnostic;
import printscript.common.token.Span;

public interface SymbolTable {
    Optional<Diagnostic> declare(String name, Type type, Span declarationSite);

    Optional<Type> lookup(String name);
}
