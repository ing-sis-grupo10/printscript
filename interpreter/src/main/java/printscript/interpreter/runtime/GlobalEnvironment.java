package printscript.interpreter.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GlobalEnvironment implements Environment {
    private final Map<String, RuntimeValue> values = new HashMap<>();

    @Override
    public void define(String name, RuntimeValue value) {
        values.put(name, value);
    }

    @Override
    public void assign(String name, RuntimeValue value) {
        values.put(name, value);
    }

    @Override
    public Optional<RuntimeValue> lookup(String name) {
        return Optional.ofNullable(values.get(name));
    }
}
