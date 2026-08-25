package printscript.semantic;

import java.util.Optional;
import printscript.common.token.Span;
import printscript.diagnostics.DiagnosticReporter;

public interface SymbolTable {
    void declare(String name, Type type, Span declarationSite, DiagnosticReporter reporter);

    Optional<Type> lookup(String name);
}
