package printscript.interpreter.runtime;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class FixedInputSource implements InputSource {
    private final Deque<String> values;

    public FixedInputSource(List<String> values) {
        this.values = new ArrayDeque<>(values);
    }

    @Override
    public String read(String prompt) {
        if (values.isEmpty()) {
            throw new IllegalStateException("No hay más valores precargados para leer");
        }
        return values.poll();
    }
}
