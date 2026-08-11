package printscript.semantic;

import edu.austral.printscript.common.token.Span;
import printscript.diagnostics.DiagnosticReporter;

import java.util.Optional;

public interface SymbolTable {
    void declare(String name, Type type, Span declarationSite, DiagnosticReporter reporter);
    Optional<Type> lookup(String name);
}