package parser;

import edu.austral.printscript.common.token.Position;
import edu.austral.printscript.common.token.Span;
import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;
import org.junit.jupiter.api.Test;
import printscript.ast.DeclaredType;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.diagnostics.CollectingDiagnosticReporter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrintScriptParserTest {

    private Token token(TokenType type, String value) {
        Span span = Span.of(new Position(1, 0), new Position(1, value.length()));
        return new Token(type, value, span);
    }

    @Test
    void parsesVariableDeclarationWithInitializer() {
        List<Token> tokens = List.of(
                token(TokenType.LET, "let"), token(TokenType.IDENTIFIER, "x"), token(TokenType.COLON, ":"),
                token(TokenType.NUMBER_TYPE, "number"), token(TokenType.ASSIGN, "="), token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.SEMICOLON, ";"), token(TokenType.EOF, "")
        );

        var reporter = new CollectingDiagnosticReporter();
        Parser parser = new PrintScriptParser(tokens.iterator(),
                List.of(new VariableDeclarationParser(), new AssignmentParser(), new PrintlnStatementParser()),
                new PrecedenceClimbingExpressionParser(), reporter);

        var declaration = (VariableDeclaration) parser.next();

        assertEquals("x", declaration.name());
        assertEquals(DeclaredType.NUMBER, declaration.declaredType());
        assertTrue(declaration.initializer().isPresent());
        assertFalse(reporter.hasErrors());
    }

    @Test
    void reportsErrorAndRecoversToNextStatement() {
        List<Token> tokens = List.of(
                token(TokenType.LET, "let"), token(TokenType.IDENTIFIER, "x"), token(TokenType.NUMBER_TYPE, "number"),
                token(TokenType.ASSIGN, "="), token(TokenType.NUMBER_LITERAL, "5"), token(TokenType.SEMICOLON, ";"),
                token(TokenType.PRINTLN, "println"), token(TokenType.LEFT_PAREN, "("), token(TokenType.IDENTIFIER, "x"),
                token(TokenType.RIGHT_PAREN, ")"), token(TokenType.SEMICOLON, ";"), token(TokenType.EOF, "")
        );

        var reporter = new CollectingDiagnosticReporter();
        Parser parser = new PrintScriptParser(tokens.iterator(),
                List.of(new VariableDeclarationParser(), new AssignmentParser(), new PrintlnStatementParser()),
                new PrecedenceClimbingExpressionParser(), reporter);

        Statement statement = parser.next();

        assertTrue(reporter.hasErrors());
        assertInstanceOf(PrintlnStatement.class, statement);
        assertFalse(parser.hasNext());
    }
}