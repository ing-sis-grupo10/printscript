package printscript.formatter;

import java.io.Reader;
import java.io.Writer;
import printscript.common.Version;

public interface Formatter {
    void format(Reader source, Writer out, Version version);

    default void format(Reader source, Writer out) {
        format(source, out, Version.latest());
    }
}
