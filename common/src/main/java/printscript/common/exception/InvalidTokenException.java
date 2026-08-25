package printscript.common.exception;

import printscript.common.token.Span;

public class InvalidTokenException extends RuntimeException {

    private final Span span;

    public InvalidTokenException(String message, Span span) {
        super(message);
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }
}
