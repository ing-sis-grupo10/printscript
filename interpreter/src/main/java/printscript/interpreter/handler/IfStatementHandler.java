package printscript.interpreter.handler;

import java.util.List;
import java.util.function.Supplier;
import printscript.ast.IfStatement;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.runtime.Environment;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.RuntimeValue;
import printscript.interpreter.runtime.RuntimeValue.BooleanValue;

public final class IfStatementHandler implements StatementHandler {
    private final ExpressionEvaluator evaluator;
    private final Supplier<HandlerRegistry> handlers;

    public IfStatementHandler(ExpressionEvaluator evaluator, Supplier<HandlerRegistry> handlers) {
        this.evaluator = evaluator;
        this.handlers = handlers;
    }

    @Override
    public boolean canHandle(Statement statement) {
        return statement instanceof IfStatement;
    }

    @Override
    public Result<Statement> handle(Statement statement, Environment environment) {
        IfStatement ifStatement = (IfStatement) statement;
        Result<RuntimeValue> condition = evaluator.evaluate(ifStatement.condition(), environment);

        if (condition instanceof Failure<RuntimeValue> f) {
            return Result.failure(f.diagnostics());
        }

        RuntimeValue value = ((Success<RuntimeValue>) condition).value();
        if (!(value instanceof BooleanValue booleanValue)) {
            return Result.failure(
                    Diagnostic.error(
                            "La condición de un if debe ser boolean",
                            ifStatement.condition().span()));
        }

        List<Statement> branch =
                booleanValue.value()
                        ? ifStatement.thenBranch()
                        : ifStatement.elseBranch().orElse(List.of());

        Environment scope = environment.child();
        for (Statement inner : branch) {
            Result<Statement> result = handlers.get().dispatch(inner, scope);
            if (result instanceof Failure<Statement> f) {
                return f;
            }
        }
        return Result.success(statement);
    }
}
