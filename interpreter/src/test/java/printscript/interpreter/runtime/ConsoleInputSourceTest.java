package printscript.interpreter.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ConsoleInputSourceTest {

    @Test
    void printsThePromptAndReturnsTheTypedLine() {
        var output = new ByteArrayOutputStream();
        var printStream = new PrintStream(output, true, StandardCharsets.UTF_8);
        var source = new ConsoleInputSource(new StringReader("mundo\n"), printStream);

        String result = source.read("Nombre:");

        assertEquals("mundo", result);
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Nombre:"));
    }
}
