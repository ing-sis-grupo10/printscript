package printscript.interpreter.runtime;

import java.util.Optional;

public interface Environment {
    void define(String name, RuntimeValue value);

    void assign(String name, RuntimeValue value);

    Optional<RuntimeValue> lookup(String name);
}
