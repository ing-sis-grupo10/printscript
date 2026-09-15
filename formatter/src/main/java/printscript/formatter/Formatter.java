package printscript.formatter;

import java.io.Reader;
import java.io.Writer;

public interface Formatter {
    void format(Reader source, Writer out, String version);

    default void format(Reader source, Writer out) {
        format(source, out, "1.1");
    }
}
