package printscript.diagnostics;

import java.util.ArrayList;
import java.util.List;

public final class CollectingDiagnosticReporter implements DiagnosticReporter {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    @Override
    public void report(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public List<Diagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR);
    }
}