package printscript.interpreter.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UncheckedIOException;

public final class ConsoleInputSource implements InputSource {
    private final BufferedReader reader;
    private final PrintStream out;

    public ConsoleInputSource(Reader reader, PrintStream out) {
        this.reader = new BufferedReader(reader);
        this.out = out;
    }

    @Override
    public String read(String prompt) {
        out.println(prompt);
        try {
            String line = reader.readLine();
            return line == null ? "" : line;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
