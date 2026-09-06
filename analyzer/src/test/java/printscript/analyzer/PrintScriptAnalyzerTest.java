package printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import printscript.ast.Assignment;
import printscript.ast.BinaryExpression;
import printscript.ast.BinaryOperator;
import printscript.ast.DeclaredType;
import printscript.ast.Identifier;
import printscript.ast.NumberLiteral;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.common.result.Diagnostic;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;

class PrintScriptAnalyzerTest {

    private final Span span = Span.of(new Position(1, 0), new Position(1, 5));

    private PrintScriptAnalyzer analyzerFor(List<Statement> statements, AnalyzerRules rules) {
        List<Result<Statement>> wrapped =
                statements.stream().map(Result::<Statement>success).toList();
        return new PrintScriptAnalyzer(wrapped.iterator(), rules);
    }

    @Test
    void acceptsCamelCaseIdentifierByDefault() {
        var declaration =
                new VariableDeclaration("miVariable", DeclaredType.NUMBER, Optional.empty(), span);

        var analyzer = analyzerFor(List.of(declaration), AnalyzerRules.defaults());
        var result = analyzer.next();

        assertInstanceOf(Success.class, result);
        assertSame(declaration, ((Success<Statement>) result).value());
        assertTrue(analyzer.diagnostics().isEmpty());
    }

    @Test
    void reportsSnakeCaseIdentifierWhenCamelCaseIsExpected() {
        var declaration =
                new VariableDeclaration("mi_variable", DeclaredType.NUMBER, Optional.empty(), span);

        var analyzer = analyzerFor(List.of(declaration), AnalyzerRules.defaults());
        var result = analyzer.next();

        assertInstanceOf(Success.class, result); // el statement sigue pasando, solo se avisa
        assertEquals(1, analyzer.diagnostics().size());
    }

    @Test
    void acceptsSnakeCaseIdentifierWhenConfigured() {
        var rules = new AnalyzerRules(AnalyzerRules.IdentifierCase.SNAKE_CASE, true);
        var declaration =
                new VariableDeclaration("mi_variable", DeclaredType.NUMBER, Optional.empty(), span);

        var analyzer = analyzerFor(List.of(declaration), rules);
        analyzer.next();

        assertTrue(analyzer.diagnostics().isEmpty());
    }

    @Test
    void acceptsPrintlnWithIdentifierArgument() {
        var println = new PrintlnStatement(new Identifier("x", span), span);

        var analyzer = analyzerFor(List.of(println), AnalyzerRules.defaults());
        analyzer.next();

        assertTrue(analyzer.diagnostics().isEmpty());
    }

    @Test
    void reportsPrintlnWithExpressionArgument() {
        var expression =
                new BinaryExpression(
                        new NumberLiteral(BigDecimal.ONE, span),
                        BinaryOperator.PLUS,
                        new NumberLiteral(BigDecimal.TWO, span),
                        span);
        var println = new PrintlnStatement(expression, span);

        var analyzer = analyzerFor(List.of(println), AnalyzerRules.defaults());
        var result = analyzer.next();

        assertInstanceOf(Success.class, result); // sigue siendo válido, solo no recomendado
        assertEquals(1, analyzer.diagnostics().size());
    }

    @Test
    void skipsPrintlnRuleWhenDisabled() {
        var rules = new AnalyzerRules(AnalyzerRules.IdentifierCase.CAMEL_CASE, false);
        var expression =
                new BinaryExpression(
                        new NumberLiteral(BigDecimal.ONE, span),
                        BinaryOperator.PLUS,
                        new NumberLiteral(BigDecimal.TWO, span),
                        span);
        var println = new PrintlnStatement(expression, span);

        var analyzer = analyzerFor(List.of(println), rules);
        analyzer.next();

        assertTrue(analyzer.diagnostics().isEmpty());
    }

    @Test
    void doesNotCheckAssignments() {
        var assignment =
                new Assignment("Mal_Nombrado", new NumberLiteral(BigDecimal.ONE, span), span);

        var analyzer = analyzerFor(List.of(assignment), AnalyzerRules.defaults());
        analyzer.next();

        assertTrue(analyzer.diagnostics().isEmpty());
    }

    @Test
    void passesThroughUpstreamFailureWithoutAnalyzing() {
        Result<Statement> upstreamFailure =
                Result.failure(Diagnostic.error("error de sintaxis previo", span));

        var analyzer =
                new PrintScriptAnalyzer(
                        List.of(upstreamFailure).iterator(), AnalyzerRules.defaults());
        var result = analyzer.next();

        assertSame(upstreamFailure, result);
        assertTrue(analyzer.diagnostics().isEmpty());
    }
}
