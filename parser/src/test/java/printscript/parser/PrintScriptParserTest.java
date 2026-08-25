package printscript.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.*;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.diagnostics.CollectingDiagnosticReporter;

class PrintScriptParserTest {

    private Token token(TokenType type, String value) {
        Span span = Span.of(new Position(1, 0), new Position(1, value.length()));
        return new Token(type, value, span);
    }

    @Test
    void parsesVariableDeclarationWithInitializer() {
        List<Token> tokens =
                List.of(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.NUMBER_TYPE, "number"),
                        token(TokenType.ASSIGN, "="),
                        token(TokenType.NUMBER_LITERAL, "5"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.EOF, ""));

        var reporter = new CollectingDiagnosticReporter();
        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);

        var declaration = (VariableDeclaration) parser.next();

        assertEquals("x", declaration.name());
        assertEquals(DeclaredType.NUMBER, declaration.declaredType());
        assertTrue(declaration.initializer().isPresent());
        assertFalse(reporter.hasErrors());
    }

    @Test
    void reportsErrorAndRecoversToNextStatement() {
        List<Token> tokens =
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

        var reporter = new CollectingDiagnosticReporter();
        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);

        Statement statement = parser.next();

        assertTrue(reporter.hasErrors());
        assertInstanceOf(PrintlnStatement.class, statement);
        assertFalse(parser.hasNext());
    }

    @Test
    void parsesAssignmentStatement() {
        List<Token> tokens =
                List.of(
                        token(TokenType.IDENTIFIER, "x"),
                        token(TokenType.ASSIGN, "="),
                        token(TokenType.NUMBER_LITERAL, "5"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.EOF, ""));

        var reporter = new CollectingDiagnosticReporter();
        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);

        var assignment = (Assignment) parser.next();

        assertEquals("x", assignment.name());
        assertFalse(reporter.hasErrors());
    }

    @Test
    void parsesBinaryExpressionsWithAllOperators() {
        // a + b - c * d / e
        List<Token> tokens =
                List.of(
                        token(TokenType.LET, "let"),
                        token(TokenType.IDENTIFIER, "r"),
                        token(TokenType.COLON, ":"),
                        token(TokenType.NUMBER_TYPE, "number"),
                        token(TokenType.ASSIGN, "="),
                        token(TokenType.IDENTIFIER, "a"),
                        token(TokenType.PLUS, "+"),
                        token(TokenType.IDENTIFIER, "b"),
                        token(TokenType.MINUS, "-"),
                        token(TokenType.IDENTIFIER, "c"),
                        token(TokenType.STAR, "*"),
                        token(TokenType.IDENTIFIER, "d"),
                        token(TokenType.SLASH, "/"),
                        token(TokenType.IDENTIFIER, "e"),
                        token(TokenType.SEMICOLON, ";"),
                        token(TokenType.EOF, ""));

        var reporter = new CollectingDiagnosticReporter();
        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);

        var declaration = (VariableDeclaration) parser.next();

        assertTrue(declaration.initializer().isPresent());
        assertFalse(reporter.hasErrors());
    }

    @Test
    void parsesStringLiteralAsExpression() {
        List<Token> tokens =
                List.of(
                        token(TokenType.PRINTLN, "println"), token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.STRING_LITERAL, "hola"), token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"), token(TokenType.EOF, ""));

        var reporter = new CollectingDiagnosticReporter();
        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);

        parser.next();

        assertFalse(reporter.hasErrors());
    }

    @Test
    void reportsErrorOnUnexpectedFactor() {
        // println(;) -- no hay expresión válida antes del ";"
        List<Token> tokens =
                List.of(
                        token(TokenType.PRINTLN, "println"), token(TokenType.LEFT_PAREN, "("),
                        token(TokenType.SEMICOLON, ";"), token(TokenType.RIGHT_PAREN, ")"),
                        token(TokenType.SEMICOLON, ";"), token(TokenType.EOF, ""));

        var reporter = new CollectingDiagnosticReporter();
        PrintScriptParser parser =
                new PrintScriptParser(
                        tokens.iterator(),
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);

        parser.next();

        assertTrue(reporter.hasErrors());
    }
}
