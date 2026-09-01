package printscript.interpreter.runtime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.function.BinaryOperator;
import printscript.ast.BinaryExpression;
import printscript.ast.DeclaredType;
import printscript.ast.Expression;
import printscript.ast.Identifier;
import printscript.ast.NumberLiteral;
import printscript.ast.StringLiteral;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.runtime.RuntimeValue.NumberValue;
import printscript.interpreter.runtime.RuntimeValue.StringValue;

public final class ExpressionEvaluator {

    public Result<RuntimeValue> evaluate(Expression expression, Environment environment) {
        return switch (expression) {
            case NumberLiteral n -> Result.success(new NumberValue(n.value()));
            case StringLiteral s -> Result.success(new StringValue(s.value()));
            case Identifier id -> lookupIdentifier(id, environment);
            case BinaryExpression b -> evaluateBinary(b, environment);
        };
    }

    private Result<RuntimeValue> lookupIdentifier(Identifier id, Environment environment) {
        Optional<RuntimeValue> value = environment.valueOf(id.name());
        if (value.isPresent()) {
            return Result.success(value.get());
        }
        if (environment.typeOf(id.name()).isPresent()) {
            return Result.failure(
                    Diagnostic.error("Variable usada sin inicializar: " + id.name(), id.span()));
        }
        return Result.failure(Diagnostic.error("Variable no declarada: " + id.name(), id.span()));
    }

    private Result<RuntimeValue> evaluateBinary(
            BinaryExpression expression, Environment environment) {
        Result<RuntimeValue> leftResult = evaluate(expression.left(), environment);
        if (leftResult instanceof Failure<RuntimeValue> lf) {
            return lf;
        }
        Result<RuntimeValue> rightResult = evaluate(expression.right(), environment);
        if (rightResult instanceof Failure<RuntimeValue> rf) {
            return rf;
        }

        RuntimeValue left = ((Success<RuntimeValue>) leftResult).value();
        RuntimeValue right = ((Success<RuntimeValue>) rightResult).value();

        return switch (expression.operator()) {
            case PLUS -> Result.success(evaluatePlus(left, right));
            case MINUS -> applyArithmetic(left, right, expression, BigDecimal::subtract);
            case TIMES -> applyArithmetic(left, right, expression, BigDecimal::multiply);
            case DIVIDE -> evaluateDivide(left, right, expression);
        };
    }

    private RuntimeValue evaluatePlus(RuntimeValue left, RuntimeValue right) {
        if (left instanceof StringValue || right instanceof StringValue) {
            return new StringValue(display(left) + display(right));
        }
        return new NumberValue(numberOf(left).add(numberOf(right)));
    }

    private Result<RuntimeValue> applyArithmetic(
            RuntimeValue left,
            RuntimeValue right,
            BinaryExpression expression,
            BinaryOperator<BigDecimal> operation) {
        if (!(left instanceof NumberValue l) || !(right instanceof NumberValue r)) {
            return Result.failure(
                    Diagnostic.error(
                            "Los operandos de " + expression.operator() + " deben ser number",
                            expression.span()));
        }
        return Result.success(new NumberValue(operation.apply(l.value(), r.value())));
    }

    private Result<RuntimeValue> evaluateDivide(
            RuntimeValue left, RuntimeValue right, BinaryExpression expression) {
        if (!(left instanceof NumberValue l) || !(right instanceof NumberValue r)) {
            return Result.failure(
                    Diagnostic.error(
                            "Los operandos de " + expression.operator() + " deben ser number",
                            expression.span()));
        }
        if (r.value().compareTo(BigDecimal.ZERO) == 0) {
            return Result.failure(Diagnostic.error("División por cero", expression.span()));
        }
        return Result.success(new NumberValue(l.value().divide(r.value(), MathContext.DECIMAL64)));
    }

    private BigDecimal numberOf(RuntimeValue value) {
        return ((NumberValue) value).value();
    }

    public DeclaredType typeOf(RuntimeValue value) {
        return switch (value) {
            case NumberValue n -> DeclaredType.NUMBER;
            case StringValue s -> DeclaredType.STRING;
        };
    }

    public String display(RuntimeValue value) {
        return switch (value) {
            case NumberValue n -> n.value().stripTrailingZeros().toPlainString();
            case StringValue s -> s.value();
        };
    }
}
