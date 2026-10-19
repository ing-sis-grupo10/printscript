package printscript.common.token;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TokenTest {

    @Test
    void mergeTakesStartOfFirstAndEndOfSecond() {
        Span first = Span.of(new Position(0, 0), new Position(0, 1));
        Span second = Span.of(new Position(0, 4), new Position(0, 5));

        Span merged = Span.merge(first, second);

        assertEquals(new Position(0, 0), merged.start());
        assertEquals(new Position(0, 5), merged.end());
    }

    @Test
    void tokenKeepsTypeValueAndSpan() {
        Span span = Span.of(new Position(2, 3), new Position(2, 6));
        Token token = new Token(TokenType.LET, "let", span);

        assertEquals(TokenType.LET, token.type());
        assertEquals("let", token.value());
        assertEquals(2, token.span().start().line());
        assertEquals(6, token.span().end().column());
    }
}
