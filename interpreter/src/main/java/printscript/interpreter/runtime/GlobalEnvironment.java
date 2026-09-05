package printscript.interpreter.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import printscript.ast.DeclaredType;
import printscript.common.result.Diagnostic;
import printscript.common.token.Span;

public final class GlobalEnvironment implements Environment {
    private record Variable(DeclaredType type, Optional<RuntimeValue> value) {}

    private final Map<String, Variable> variables = new HashMap<>();

    @Override
    public Optional<Diagnostic> declare(String name, DeclaredType type, Span declarationSite) {
        if (variables.containsKey(name)) {
            return Optional.of(Diagnostic.error("Variable ya declarada: " + name, declarationSite));
        }
        variables.put(name, new Variable(type, Optional.empty()));
        return Optional.empty();
    }

    @Override
    public void assign(String name, RuntimeValue value) {
        Variable current = variables.get(name);
        variables.put(name, new Variable(current.type(), Optional.of(value)));
    }

    @Override
    public Optional<DeclaredType> typeOf(String name) {
        return Optional.ofNullable(variables.get(name)).map(Variable::type);
    }

    @Override
    public Optional<RuntimeValue> valueOf(String name) {
        return Optional.ofNullable(variables.get(name)).flatMap(Variable::value);
    }
}
