package printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class TokenStreamTest {

    @Test
    void synthesizesEofWhenSourceRunsOutWithoutExplicitEof() {
        // a propósito, sin token EOF al final
        List<Token> tokens = List.of(new Token(TokenType.SEMICOLON, ";", null));
        TokenStream stream = new TokenStream(tokens.iterator());

        stream.consume(); // consume el ";", el iterator queda vacío
        Token synthesized = stream.peek();

        assertEquals(TokenType.EOF, synthesized.type());
    }
}
