package printscript.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.common.Version;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.finders.NumberFinder;

class LexerFactoryTest {

    private TokenType firstType(PrintScriptLexer lexer) {
        Result<Token> result = lexer.next();
        assertInstanceOf(Success.class, result);
        return ((Success<Token>) result).value().type();
    }

    @Test
    void versionOneDotZeroTreatsConstAsIdentifier() {
        var lexer = LexerFactory.create(new StringReader("const"), Version.V1_0);
        assertEquals(TokenType.IDENTIFIER, firstType(lexer));
    }

    @Test
    void versionOneDotOneRecognizesConstKeyword() {
        var lexer = LexerFactory.create(new StringReader("const"), Version.V1_1);
        assertEquals(TokenType.CONST, firstType(lexer));
    }

    @Test
    void usesOnlyTheInjectedFinders() {
        var numbersOnly = new PrintScriptLexer(new StringReader("42"), List.of(new NumberFinder()));
        assertEquals(TokenType.NUMBER_LITERAL, firstType(numbersOnly));

        var cannotReadLetters =
                new PrintScriptLexer(new StringReader("let"), List.of(new NumberFinder()));
        assertInstanceOf(Failure.class, cannotReadLetters.next());
    }
}
