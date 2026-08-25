package printscript.diagnostics;

import printscript.common.token.Position;
import printscript.common.token.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectingDiagnosticReporterTest {

    @Test
    void collectsMultipleDiagnostics() {
        var reporter = new CollectingDiagnosticReporter();
        var span = Span.of(new Position(1, 0), new Position(1, 5));

        reporter.report(Diagnostic.error("primer error", span));
        reporter.report(Diagnostic.warning("un warning", span));

        assertEquals(2, reporter.diagnostics().size());
    }

    @Test
    void hasErrorsIsFalseWhenOnlyWarnings() {
        var reporter = new CollectingDiagnosticReporter();
        var span = Span.of(new Position(1, 0), new Position(1, 5));

        reporter.report(Diagnostic.warning("solo un warning", span));

        assertFalse(reporter.hasErrors());
    }

    @Test
    void hasErrorsIsTrueWhenThereIsAtLeastOneError() {
        var reporter = new CollectingDiagnosticReporter();
        var span = Span.of(new Position(1, 0), new Position(1, 5));

        reporter.report(Diagnostic.error("esto sí es un error", span));

        assertTrue(reporter.hasErrors());
    }
}