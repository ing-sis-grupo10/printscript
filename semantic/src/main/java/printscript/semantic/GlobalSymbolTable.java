package printscript.semantic;

import edu.austral.printscript.common.token.Span;
import printscript.diagnostics.Diagnostic;
import printscript.diagnostics.DiagnosticReporter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GlobalSymbolTable implements SymbolTable {
    private final Map<String, Type> variables = new HashMap<>();

    @Override
    public void declare(String name, Type type, Span declarationSite, DiagnosticReporter reporter) {
        if (variables.containsKey(name)) {
            reporter.report(Diagnostic.error("Variable ya declarada: " + name, declarationSite));
            return;
        }
        variables.put(name, type);
    }

    @Override
    public Optional<Type> lookup(String name) {
        return Optional.ofNullable(variables.get(name));
    }
}