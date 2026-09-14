package printscript.interpreter.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import printscript.ast.DeclaredType;
import printscript.ast.NumberLiteral;
import printscript.ast.ReadEnvExpression;
import printscript.ast.ReadInputExpression;
import printscript.ast.StringLiteral;
import printscript.common.result.Failure;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.interpreter.runtime.RuntimeValue.BooleanValue;
import printscript.interpreter.runtime.RuntimeValue.NumberValue;
import printscript.interpreter.runtime.RuntimeValue.StringValue;

class ExpressionEvaluatorTest {

    private final Span span = Span.of(new Position(1, 0), new Position(1, 5));
    private final Environment environment = new GlobalEnvironment();

    @Test
    void readInputReturnsValueFromInputSource() {
        var evaluator = new ExpressionEvaluator(new FixedInputSource(List.of("mundo")));
        var message = new StringLiteral("Nombre:", span);

        var result = evaluator.evaluate(new ReadInputExpression(message, span), environment);

        assertInstanceOf(Success.class, result);
        assertEquals(new StringValue("mundo"), ((Success<RuntimeValue>) result).value());
    }

    @Test
    void readInputFailsWhenMessageIsNotString() {
        var evaluator = new ExpressionEvaluator(new FixedInputSource(List.of("mundo")));
        var message = new NumberLiteral(BigDecimal.ONE, span);

        var result = evaluator.evaluate(new ReadInputExpression(message, span), environment);

        assertInstanceOf(Failure.class, result);
    }

    @Test
    void readEnvFailsWhenVariableIsNotDefined() {
        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var name = new StringLiteral("UNA_VARIABLE_QUE_SEGURO_NO_EXISTE_12345", span);

        var result = evaluator.evaluate(new ReadEnvExpression(name, span), environment);

        assertInstanceOf(Failure.class, result);
    }

    @Test
    void readEnvReturnsRealEnvironmentVariableWhenDefined() {
        String pathValue = System.getenv("PATH");
        Assumptions.assumeTrue(pathValue != null, "PATH no está definida en este entorno");

        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var name = new StringLiteral("PATH", span);

        var result = evaluator.evaluate(new ReadEnvExpression(name, span), environment);

        assertInstanceOf(Success.class, result);
        assertEquals(new StringValue(pathValue), ((Success<RuntimeValue>) result).value());
    }

    @Test
    void coerceForAssignmentPassesThroughWhenTypeAlreadyMatches() {
        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var literal = new NumberLiteral(BigDecimal.TEN, span);

        var result =
                evaluator.coerceForAssignment(
                        literal, new NumberValue(BigDecimal.TEN), DeclaredType.NUMBER, span);

        assertInstanceOf(Success.class, result);
    }

    @Test
    void coerceForAssignmentParsesStringFromReadInputIntoNumber() {
        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var source = new ReadInputExpression(new StringLiteral("Edad:", span), span);

        var result =
                evaluator.coerceForAssignment(
                        source, new StringValue("42"), DeclaredType.NUMBER, span);

        assertInstanceOf(Success.class, result);
        assertEquals(
                new NumberValue(new BigDecimal("42")), ((Success<RuntimeValue>) result).value());
    }

    @Test
    void coerceForAssignmentFailsWhenStringFromReadInputIsNotANumber() {
        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var source = new ReadInputExpression(new StringLiteral("Edad:", span), span);

        var result =
                evaluator.coerceForAssignment(
                        source, new StringValue("no es un numero"), DeclaredType.NUMBER, span);

        assertInstanceOf(Failure.class, result);
    }

    @Test
    void coerceForAssignmentParsesStringFromReadEnvIntoBoolean() {
        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var source = new ReadEnvExpression(new StringLiteral("FLAG", span), span);

        var result =
                evaluator.coerceForAssignment(
                        source, new StringValue("true"), DeclaredType.BOOLEAN, span);

        assertInstanceOf(Success.class, result);
        assertEquals(new BooleanValue(true), ((Success<RuntimeValue>) result).value());
    }

    @Test
    void coerceForAssignmentFailsWhenStringFromReadEnvIsNotABoolean() {
        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var source = new ReadEnvExpression(new StringLiteral("FLAG", span), span);

        var result =
                evaluator.coerceForAssignment(
                        source, new StringValue("quizas"), DeclaredType.BOOLEAN, span);

        assertInstanceOf(Failure.class, result);
    }

    @Test
    void coerceForAssignmentDoesNotCoercePlainStringLiterals() {
        // clave: un string literal común NUNCA se convierte, solo readInput/readEnv
        var evaluator = new ExpressionEvaluator(prompt -> prompt);
        var source = new StringLiteral("42", span);

        var result =
                evaluator.coerceForAssignment(
                        source, new StringValue("42"), DeclaredType.NUMBER, span);

        assertInstanceOf(Failure.class, result);
    }
}
