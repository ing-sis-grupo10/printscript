package printscript.common.result;

import java.util.List;

public record Failure<T>(List<Diagnostic> diagnostics) implements Result<T> {
    public Failure {
        diagnostics = List.copyOf(diagnostics);
    }
}