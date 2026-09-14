package printscript.interpreter.runtime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.function.BinaryOperator;
import printscript.ast.*;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Span;
import printscript.interpreter.runtime.RuntimeValue.BooleanValue;
import printscript.interpreter.runtime.RuntimeValue.NumberValue;
import printscript.interpreter.runtime.RuntimeValue.StringValue;

public final class ExpressionEvaluator {
    private final InputSource inputSource;

    public ExpressionEvaluator(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    public Result<RuntimeValue> evaluate(Expression expression, Environment environment) {
        return switch (expression) {
            case NumberLiteral n -> Result.success(new NumberValue(n.value()));
            case StringLiteral s -> Result.success(new StringValue(s.value()));
            case BooleanLiteral b -> Result.success(new BooleanValue(b.value()));
            case Identifier id -> lookupIdentifier(id, environment);
            case BinaryExpression b -> evaluateBinary(b, environment);
            case ReadInputExpression r -> evaluateReadInput(r, environment);
            case ReadEnvExpression r -> evaluateReadEnv(r, environment);
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

    private Result<RuntimeValue> evaluateReadInput(
            ReadInputExpression expression, Environment environment) {
        Result<RuntimeValue> messageResult = evaluate(expression.message(), environment);
        if (messageResult instanceof Failure<RuntimeValue> f) {
            return f;
        }
        RuntimeValue messageValue = ((Success<RuntimeValue>) messageResult).value();
        if (!(messageValue instanceof StringValue message)) {
            return Result.failure(
                    Diagnostic.error(
                            "El mensaje de readInput debe ser string",
                            expression.message().span()));
        }
        return Result.success(new StringValue(inputSource.read(message.value())));
    }

    private Result<RuntimeValue> evaluateReadEnv(
            ReadEnvExpression expression, Environment environment) {
        Result<RuntimeValue> nameResult = evaluate(expression.name(), environment);
        if (nameResult instanceof Failure<RuntimeValue> f) {
            return f;
        }
        RuntimeValue nameValue = ((Success<RuntimeValue>) nameResult).value();
        if (!(nameValue instanceof StringValue name)) {
            return Result.failure(
                    Diagnostic.error(
                            "El nombre de readEnv debe ser string", expression.name().span()));
        }
        String value = System.getenv(name.value());
        if (value == null) {
            return Result.failure(
                    Diagnostic.error(
                            "Variable de entorno no definida: " + name.value(), expression.span()));
        }
        return Result.success(new StringValue(value));
    }

    private BigDecimal numberOf(RuntimeValue value) {
        return ((NumberValue) value).value();
    }

    public DeclaredType typeOf(RuntimeValue value) {
        return switch (value) {
            case NumberValue n -> DeclaredType.NUMBER;
            case StringValue s -> DeclaredType.STRING;
            case BooleanValue b -> DeclaredType.BOOLEAN;
        };
    }

    public String display(RuntimeValue value) {
        return switch (value) {
            case NumberValue n -> n.value().stripTrailingZeros().toPlainString();
            case StringValue s -> s.value();
            case BooleanValue b -> String.valueOf(b.value());
        };
    }

    public Result<RuntimeValue> coerceForAssignment(
            Expression source, RuntimeValue value, DeclaredType expectedType, Span span) {
        if (typeOf(value) == expectedType) {
            return Result.success(value);
        }

        boolean allowsCoercion =
                source instanceof ReadInputExpression || source instanceof ReadEnvExpression;
        if (allowsCoercion && value instanceof StringValue s) {
            Optional<RuntimeValue> coerced = tryCoerce(s.value(), expectedType);
            if (coerced.isPresent()) {
                return Result.success(coerced.get());
            }
            return Result.failure(
                    Diagnostic.error(
                            "No se pudo convertir \"" + s.value() + "\" a " + expectedType, span));
        }

        return Result.failure(
                Diagnostic.error(
                        "No se puede asignar "
                                + typeOf(value)
                                + " a una variable de tipo "
                                + expectedType,
                        span));
    }

    private Optional<RuntimeValue> tryCoerce(String raw, DeclaredType expectedType) {
        return switch (expectedType) {
            case NUMBER -> tryParseNumber(raw);
            case BOOLEAN -> tryParseBoolean(raw);
            case STRING -> Optional.of(new StringValue(raw));
        };
    }

    private Optional<RuntimeValue> tryParseNumber(String raw) {
        try {
            return Optional.of(new NumberValue(new BigDecimal(raw)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<RuntimeValue> tryParseBoolean(String raw) {
        if (raw.equals("true")) {
            return Optional.of(new BooleanValue(true));
        }
        if (raw.equals("false")) {
            return Optional.of(new BooleanValue(false));
        }
        return Optional.empty();
    }
}
