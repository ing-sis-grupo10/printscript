package printscript.semantic;

import printscript.common.result.Diagnostic;
import printscript.common.token.Span;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GlobalSymbolTable implements SymbolTable {
    private final Map<String, Type> variables = new HashMap<>();

    @Override
    public Optional<Diagnostic> declare(String name, Type type, Span declarationSite) {
        if (variables.containsKey(name)) {
            return Optional.of(Diagnostic.error("Variable ya declarada: " + name, declarationSite));
        }
        variables.put(name, type);
        return Optional.empty();
    }

    @Override
    public Optional<Type> lookup(String name) {
        return Optional.ofNullable(variables.get(name));
    }
}