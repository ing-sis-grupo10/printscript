package printscript.interpreter.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class FixedInputSourceTest {

    @Test
    void returnsValuesInOrder() {
        var source = new FixedInputSource(List.of("uno", "dos"));

        assertEquals("uno", source.read("prompt"));
        assertEquals("dos", source.read("prompt"));
    }

    @Test
    void throwsWhenExhausted() {
        var source = new FixedInputSource(List.of());

        assertThrows(IllegalStateException.class, () -> source.read("prompt"));
    }
}
