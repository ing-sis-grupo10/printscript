package printscript.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.Identifier;
import printscript.ast.IfStatement;
import printscript.ast.PrintlnStatement;
import printscript.common.result.Result;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class IfStatementParserTest {

    private Result<Token> token(TokenType type, String value) {
        Span span = Span.of(new Position(1, 0), new Position(1, value.length()));
        return Result.success(new Token(type, value, span));
    }

    private TokenStream streamOf(List<Result<Token>> tokens) {
        return new TokenStream(tokens.iterator());
    }

    private IfStatementParser buildParser() {
        List<StatementParser> parsers = new ArrayList<>();
        parsers.add(new VariableDeclarationParser());
        parsers.add(new AssignmentParser());
        parsers.add(new PrintlnStatementParser());
        IfStatementParser ifParser = new IfStatementParser(() -> parsers);
        parsers.add(ifParser);
        return ifParser;
    }

    @Test
    void parsesIfWithoutElse() {
        List<Result<Token>> tokens =
                List.of(
                        token(TokenType.IF, "if"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "esValido"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.PRINTLN, "println"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "esValido"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.RIGHT_BRACE, "}"));

        var ifParser = buildParser();
        var statement =
                (IfStatement)
                        ifParser.parse(streamOf(tokens), new PrecedenceClimbingExpressionParser());

        assertInstanceOf(Identifier.class, statement.condition());
        assertEquals("esValido", ((Identifier) statement.condition()).name());
        assertEquals(1, statement.thenBranch().size());
        assertInstanceOf(PrintlnStatement.class, statement.thenBranch().get(0));
        assertTrue(statement.elseBranch().isEmpty());
    }

    @Test
    void parsesIfWithElse() {
        List<Result<Token>> tokens =
                List.of(
                        token(TokenType.IF, "if"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "esValido"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.PRINTLN, "println"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "esValido"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.RIGHT_BRACE, "}"),
                        token(TokenType.ELSE, "else"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.PRINTLN, "println"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "esValido"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.RIGHT_BRACE, "}"));

        var ifParser = buildParser();
        var statement =
                (IfStatement)
                        ifParser.parse(streamOf(tokens), new PrecedenceClimbingExpressionParser());

        assertTrue(statement.elseBranch().isPresent());
        assertEquals(1, statement.elseBranch().get().size());
    }

    @Test
    void parsesNestedIfInsideIf() {
        List<Result<Token>> tokens =
                List.of(
                        token(TokenType.IF, "if"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "a"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.IF, "if"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "b"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.PRINTLN, "println"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "b"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.RIGHT_BRACE, "}"),
                        token(TokenType.RIGHT_BRACE, "}"));

        var ifParser = buildParser();
        var outer =
                (IfStatement)
                        ifParser.parse(streamOf(tokens), new PrecedenceClimbingExpressionParser());

        assertEquals(1, outer.thenBranch().size());
        assertInstanceOf(IfStatement.class, outer.thenBranch().get(0));
    }

    @Test
    void rejectsLiteralAsCondition() {
        List<Result<Token>> tokens =
                List.of(
                        token(TokenType.IF, "if"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.TRUE, "true"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.RIGHT_BRACE, "}"));

        var ifParser = buildParser();
        assertThrows(
                ParseError.class,
                () -> ifParser.parse(streamOf(tokens), new PrecedenceClimbingExpressionParser()));
    }

    @Test
    void rejectsElseIfChaining() {
        List<Result<Token>> tokens =
                List.of(
                        token(TokenType.IF, "if"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "a"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.RIGHT_BRACE, "}"),
                        token(TokenType.ELSE, "else"),
                        token(TokenType.IF, "if"),
                        token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.IDENTIFIER, "b"),
                        token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.LEFT_BRACE, "{"),
                        token(TokenType.RIGHT_BRACE, "}"));

        var ifParser = buildParser();
        assertThrows(
                ParseError.class,
                () -> ifParser.parse(streamOf(tokens), new PrecedenceClimbingExpressionParser()));
    }
}
