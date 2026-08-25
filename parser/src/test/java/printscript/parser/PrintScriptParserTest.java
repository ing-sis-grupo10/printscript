package printscript.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.DeclaredType;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class PrintScriptParserTest {

    private Result<Token> token(TokenType type, String value) {
        Span span = Span.of(new Position(1, 0), new Position(1, value.length()));
        return Result.success(new Token(type, value, span));
    }

    @Test
    void parsesVariableDeclarationWithInitializer() {
        List<Result<Token>> tokens =
                List.of(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.NUMBER_TYPE, "number"),
                        token(TokenType.ASSIGN, "="),
                        token(TokenType.NUMBER_LITERAL, "5"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.EOF, ""));

        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser());

        Result<Statement> result = parser.next();

        assertInstanceOf(Success.class, result);
        var declaration = (VariableDeclaration) ((Success<Statement>) result).value();

        assertEquals("x", declaration.name());
        assertEquals(DeclaredType.NUMBER, declaration.declaredType());
        assertTrue(declaration.initializer().isPresent());
    }

    @Test
    void reportsErrorAndRecoversToNextStatement() {
        List<Result<Token>> tokens =
                List.of(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.NUMBER_TYPE, "number"),
                        token(TokenType.ASSIGN, "="),
                        token(TokenType.NUMBER_LITERAL, "5"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.PRINTLN, "println"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.EOF, ""));

        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser());

        Result<Statement> first = parser.next();
        assertInstanceOf(Failure.class, first);

        Result<Statement> second = parser.next();
        assertInstanceOf(Success.class, second);
        assertInstanceOf(PrintlnStatement.class, ((Success<Statement>) second).value());

        assertFalse(parser.hasNext());
    }
}
