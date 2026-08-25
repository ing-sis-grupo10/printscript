package printscript.semantic;

import printscript.ast.Assignment;
import printscript.ast.BinaryExpression;
import printscript.ast.BinaryOperator;
import printscript.ast.DeclaredType;
import printscript.ast.NumberLiteral;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.ast.StringLiteral;
import printscript.ast.VariableDeclaration;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PrintScriptSemanticAnalyzerTest {

    private final Span span = Span.of(new Position(1, 0), new Position(1, 5));

    private PrintScriptSemanticAnalyzer analyzerFor(List<Statement> statements) {
        List<Result<Statement>> wrapped = statements.stream().map(Result::<Statement>success).toList();
        return new PrintScriptSemanticAnalyzer(wrapped.iterator(), new GlobalSymbolTable());
    }

    @Test
    void acceptsDeclarationWithMatchingType() {
        var declaration = new VariableDeclaration("x", DeclaredType.NUMBER,
                Optional.of(new NumberLiteral(BigDecimal.TEN, span)), span);

        var analyzer = analyzerFor(List.of(declaration));

        assertInstanceOf(Success.class, analyzer.next());
    }

    @Test
    void reportsTypeMismatchOnDeclaration() {
        var declaration = new VariableDeclaration("x", DeclaredType.NUMBER,
                Optional.of(new StringLiteral("hola", span)), span);

        var analyzer = analyzerFor(List.of(declaration));

        assertInstanceOf(Failure.class, analyzer.next());
    }

    @Test
    void reportsUndeclaredVariableInAssignment() {
        var assignment = new Assignment("x", new NumberLiteral(BigDecimal.ONE, span), span);

        var analyzer = analyzerFor(List.of(assignment));

        assertInstanceOf(Failure.class, analyzer.next());
    }

    @Test
    void concatenatesNumberAndStringAsString() {
        var declaration = new VariableDeclaration("x", DeclaredType.NUMBER, Optional.empty(), span);
        var concat = new BinaryExpression(
                new StringLiteral("Result: ", span), BinaryOperator.PLUS, new NumberLiteral(BigDecimal.TEN, span), span);
        var println = new PrintlnStatement(concat, span);

        var analyzer = analyzerFor(List.of(declaration, println));

        assertInstanceOf(Success.class, analyzer.next());
        assertInstanceOf(Success.class, analyzer.next());
    }

    @Test
    void reportsArithmeticWithStringOperand() {
        var expression = new BinaryExpression(
                new StringLiteral("hola", span), BinaryOperator.MINUS, new NumberLiteral(BigDecimal.ONE, span), span);
        var println = new PrintlnStatement(expression, span);

        var analyzer = analyzerFor(List.of(println));

        assertInstanceOf(Failure.class, analyzer.next());
    }

    @Test
    void reportsRedeclaration() {
        var first = new VariableDeclaration("x", DeclaredType.NUMBER, Optional.empty(), span);
        var second = new VariableDeclaration("x", DeclaredType.STRING, Optional.empty(), span);

        var analyzer = analyzerFor(List.of(first, second));

        assertInstanceOf(Success.class, analyzer.next());
        assertInstanceOf(Failure.class, analyzer.next());
    }
}