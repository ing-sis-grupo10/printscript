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

    private final Optional<Environment> parent;

    public GlobalEnvironment() {
        this.parent = Optional.empty();
    }

    private GlobalEnvironment(Environment parent) {
        this.parent = Optional.of(parent);
    }

    @Override
    public Environment child() {
        return new GlobalEnvironment(this);
    }

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
        if (variables.containsKey(name)) {
            Variable current = variables.get(name);
            variables.put(name, new Variable(current.type(), Optional.of(value)));
            return;
        }
        parent.ifPresent(p -> p.assign(name, value));
    }

    @Override
    public Optional<DeclaredType> typeOf(String name) {
        Variable local = variables.get(name);
        if (local != null) {
            return Optional.of(local.type());
        }
        return parent.flatMap(p -> p.typeOf(name));
    }

    @Override
    public Optional<RuntimeValue> valueOf(String name) {
        Variable local = variables.get(name);
        if (local != null) {
            return local.value();
        }
        return parent.flatMap(p -> p.valueOf(name));
    }
}
