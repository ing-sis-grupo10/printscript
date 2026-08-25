package printscript.interpreter.runtime;

import java.math.BigDecimal;
import java.math.MathContext;
import printscript.ast.*;
import printscript.diagnostics.Diagnostic;
import printscript.diagnostics.DiagnosticReporter;
import printscript.interpreter.runtime.RuntimeValue.NumberValue;
import printscript.interpreter.runtime.RuntimeValue.StringValue;

public final class ExpressionEvaluator {

    public RuntimeValue evaluate(
            Expression expression, Environment environment, DiagnosticReporter reporter) {
        return switch (expression) {
            case NumberLiteral n -> new NumberValue(n.value());
            case StringLiteral s -> new StringValue(s.value());
            case Identifier id ->
                    environment
                            .lookup(id.name())
                            .orElse(
                                    new NumberValue(
                                            BigDecimal.ZERO)); // semantic ya valid4 esto antes
            case BinaryExpression b -> evaluateBinary(b, environment, reporter);
        };
    }

    private RuntimeValue evaluateBinary(
            BinaryExpression expression, Environment environment, DiagnosticReporter reporter) {
        RuntimeValue left = evaluate(expression.left(), environment, reporter);
        RuntimeValue right = evaluate(expression.right(), environment, reporter);

        return switch (expression.operator()) {
            case PLUS -> evaluatePlus(left, right);
            case MINUS -> new NumberValue(numberOf(left).subtract(numberOf(right)));
            case TIMES -> new NumberValue(numberOf(left).multiply(numberOf(right)));
            case DIVIDE -> evaluateDivide(numberOf(left), numberOf(right), expression, reporter);
        };
    }

    private RuntimeValue evaluatePlus(RuntimeValue left, RuntimeValue right) {
        if (left instanceof StringValue || right instanceof StringValue) {
            return new StringValue(display(left) + display(right));
        }
        return new NumberValue(numberOf(left).add(numberOf(right)));
    }

    private RuntimeValue evaluateDivide(
            BigDecimal left,
            BigDecimal right,
            BinaryExpression expression,
            DiagnosticReporter reporter) {
        if (right.compareTo(BigDecimal.ZERO) == 0) {
            reporter.report(Diagnostic.error("División por cero", expression.span()));
            return new NumberValue(BigDecimal.ZERO);
        }
        return new NumberValue(left.divide(right, MathContext.DECIMAL64));
    }

    private BigDecimal numberOf(RuntimeValue value) {
        return ((NumberValue) value).value();
    }

    public String display(RuntimeValue value) {
        return switch (value) {
            case NumberValue n -> n.value().stripTrailingZeros().toPlainString();
            case StringValue s -> s.value();
        };
    }
}
