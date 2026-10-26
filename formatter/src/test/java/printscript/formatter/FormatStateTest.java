package printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class FormatStateTest {

    private Token token(TokenType type) {
        return new Token(type, "", Span.of(new Position(0, 0), new Position(0, 0)));
    }

    @Test
    void previousIsEmptyUntilTheFirstToken() {
        var state = new FormatState(2);
        assertTrue(state.previous().isEmpty());

        state.afterToken(token(TokenType.LET));

        assertEquals(TokenType.LET, state.previous().orElseThrow().type());
    }

    @Test
    void bracesOpenAndCloseIndentation() {
        var state = new FormatState(2);

        state.afterToken(token(TokenType.LEFT_BRACE));
        assertEquals("\n  ", state.context("").lineBreak(1));

        state.beforeToken(token(TokenType.RIGHT_BRACE));
        assertEquals("\n", state.context("").lineBreak(1));
    }

    @Test
    void remembersWhetherThePreviousStatementWasPrintln() {
        var state = new FormatState(2);

        state.afterToken(token(TokenType.PRINTLN));
        state.afterToken(token(TokenType.SEMICOLON));
        assertTrue(state.context("").previousStatementWasPrintln());

        state.afterToken(token(TokenType.LET));
        state.afterToken(token(TokenType.SEMICOLON));
        assertFalse(state.context("").previousStatementWasPrintln());
    }
}
