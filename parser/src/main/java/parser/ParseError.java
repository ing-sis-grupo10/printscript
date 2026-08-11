package parser;

import edu.austral.printscript.common.token.Span;

final class ParseError extends RuntimeException {
    private final Span span;

    ParseError(String message, Span span) {
        super(message);
        this.span = span;
    }

    Span span() {
        return span;
    }
}