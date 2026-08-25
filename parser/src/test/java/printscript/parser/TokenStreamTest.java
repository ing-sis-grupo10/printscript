package printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.common.result.Result;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class TokenStreamTest {

    @Test
    void synthesizesEofWhenSourceRunsOutWithoutExplicitEof() {
        // a propósito, sin token EOF al final
        List<Result<Token>> tokens =
                List.of(Result.success(new Token(TokenType.SEMICOLON, ";", null)));
        TokenStream stream = new TokenStream(tokens.iterator());

        stream.consume();
        Token synthesized = stream.peek();

        assertEquals(TokenType.EOF, synthesized.type());
    }
}
