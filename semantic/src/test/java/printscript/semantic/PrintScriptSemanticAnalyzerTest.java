package printscript.semantic;

import printscript.common.token.Position;
import printscript.common.token.Span;
import org.junit.jupiter.api.Test;
import printscript.ast.*;
import printscript.diagnostics.CollectingDiagnosticReporter;
import printscript.semantic.GlobalSymbolTable;
import printscript.semantic.PrintScriptSemanticAnalyzer;
import printscript.semantic.SemanticAnalyzer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PrintScriptSemanticAnalyzerTest {

    private final Span span = Span.of(new Position(1, 0), new Position(1, 5));

    private SemanticAnalyzer analyzerFor(List<Statement> statements, CollectingDiagnosticReporter reporter) {
        return new PrintScriptSemanticAnalyzer(statements.iterator(), new GlobalSymbolTable(), reporter);
    }

    @Test
    void acceptsDeclarationWithMatchingType() {
        var declaration = new VariableDeclaration("x", DeclaredType.NUMBER,
                Optional.of(new NumberLiteral(BigDecimal.TEN, span)), span);

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = analyzerFor(List.of(declaration), reporter);

        analyzer.next();

        assertFalse(reporter.hasErrors());
    }

    @Test
    void reportsTypeMismatchOnDeclaration() {
        var declaration = new VariableDeclaration("x", DeclaredType.NUMBER,
                Optional.of(new StringLiteral("hola", span)), span);

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = analyzerFor(List.of(declaration), reporter);

        analyzer.next();

        assertTrue(reporter.hasErrors());
    }

    @Test
    void reportsUndeclaredVariableInAssignment() {
        var assignment = new Assignment("x", new NumberLiteral(BigDecimal.ONE, span), span);

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = analyzerFor(List.of(assignment), reporter);

        analyzer.next();

        assertTrue(reporter.hasErrors());
    }

    @Test
    void concatenatesNumberAndStringAsString() {
        var declaration = new VariableDeclaration("x", DeclaredType.NUMBER, Optional.empty(), span);
        var concat = new BinaryExpression(
                new StringLiteral("Result: ", span), BinaryOperator.PLUS, new NumberLiteral(BigDecimal.TEN, span), span);
        var println = new PrintlnStatement(concat, span);

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = analyzerFor(List.of(declaration, println), reporter);

        analyzer.next();
        analyzer.next();

        assertFalse(reporter.hasErrors());
    }

    @Test
    void reportsArithmeticWithStringOperand() {
        var expression = new BinaryExpression(
                new StringLiteral("hola", span), BinaryOperator.MINUS, new NumberLiteral(BigDecimal.ONE, span), span);
        var println = new PrintlnStatement(expression, span);

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = analyzerFor(List.of(println), reporter);

        analyzer.next();

        assertTrue(reporter.hasErrors());
    }

    @Test
    void reportsRedeclaration() {
        var first = new VariableDeclaration("x", DeclaredType.NUMBER, Optional.empty(), span);
        var second = new VariableDeclaration("x", DeclaredType.STRING, Optional.empty(), span);

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = analyzerFor(List.of(first, second), reporter);

        analyzer.next();
        analyzer.next();

        assertTrue(reporter.hasErrors());
    }
}