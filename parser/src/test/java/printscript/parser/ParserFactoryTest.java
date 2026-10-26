package printscript.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.IfStatement;
import printscript.ast.Statement;
import printscript.common.Version;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class ParserFactoryTest {

    private Result<Token> token(TokenType type, String value) {
        Span span = Span.of(new Position(0, 0), new Position(0, value.length()));
        return Result.success(new Token(type, value, span));
    }

    private List<Result<Token>> ifTokens() {
        return List.of(
                token(TokenType.IF, "if"),
                token(TokenType.LEFT_PAREN, "("),
                token(TokenType.IDENTIFIER, "flag"),
                token(TokenType.RIGHT_PAREN, ")"),
                token(TokenType.LEFT_BRACE, "{"),
                token(TokenType.RIGHT_BRACE, "}"),
                token(TokenType.EOF, ""));
    }

    @Test
    void versionOneDotOneParsesIf() {
        var parser = ParserFactory.create(ifTokens().iterator(), Version.V1_1);

        Result<Statement> result = parser.next();

        assertInstanceOf(Success.class, result);
        assertInstanceOf(IfStatement.class, ((Success<Statement>) result).value());
    }

    @Test
    void versionOneDotZeroHasNoIfParser() {
        var parser = ParserFactory.create(ifTokens().iterator(), Version.V1_0);

        assertInstanceOf(Failure.class, parser.next());
    }
}
