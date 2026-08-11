package printscript.diagnostics;

import edu.austral.printscript.common.token.Span;


public record Diagnostic(Severity severity, String message, Span span) {

    public static Diagnostic error(String message, Span span) {
        return new Diagnostic(Severity.ERROR, message, span);
    }

    public static Diagnostic warning(String message, Span span) {
        return new Diagnostic(Severity.WARNING, message, span);
    }

    public static Diagnostic info(String message, Span span) {
        return new Diagnostic(Severity.INFO, message, span);
    }
}
