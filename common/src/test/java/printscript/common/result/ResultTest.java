package printscript.common.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.common.token.Position;
import printscript.common.token.Span;

class ResultTest {

    private final Span span = Span.of(new Position(1, 2), new Position(1, 5));

    @Test
    void diagnosticFactoriesSetSeverity() {
        assertEquals(Severity.ERROR, Diagnostic.error("e", span).severity());
        assertEquals(Severity.WARNING, Diagnostic.warning("w", span).severity());
        assertEquals(Severity.INFO, Diagnostic.info("i", span).severity());
        assertEquals("e", Diagnostic.error("e", span).message());
        assertEquals(span, Diagnostic.error("e", span).span());
    }

    @Test
    void successWrapsValue() {
        Result<String> result = Result.success("ok");
        assertInstanceOf(Success.class, result);
        assertEquals("ok", ((Success<String>) result).value());
    }

    @Test
    void failureWithSingleDiagnostic() {
        Diagnostic diagnostic = Diagnostic.error("falló", span);
        Result<String> result = Result.failure(diagnostic);
        assertInstanceOf(Failure.class, result);
        assertEquals(List.of(diagnostic), ((Failure<String>) result).diagnostics());
    }

    @Test
    void failureCopiesDiagnosticsDefensively() {
        List<Diagnostic> diagnostics = new ArrayList<>();
        diagnostics.add(Diagnostic.error("a", span));
        Failure<String> failure = (Failure<String>) Result.<String>failure(diagnostics);

        diagnostics.add(Diagnostic.error("b", span));

        assertEquals(1, failure.diagnostics().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> failure.diagnostics().add(Diagnostic.error("c", span)));
    }
}
