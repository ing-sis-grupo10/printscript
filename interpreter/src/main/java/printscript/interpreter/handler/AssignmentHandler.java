package printscript.interpreter.handler;

import printscript.ast.Assignment;
import printscript.ast.Statement;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.runtime.Environment;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.RuntimeValue;

public final class AssignmentHandler implements StatementHandler {
    private final ExpressionEvaluator evaluator;

    public AssignmentHandler(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public boolean canHandle(Statement statement) {
        return statement instanceof Assignment;
    }

    @Override
    public Result<Statement> handle(Statement statement, Environment environment) {
        Assignment assignment = (Assignment) statement;
        Result<RuntimeValue> value = evaluator.evaluate(assignment.value(), environment);
        return switch (value) {
            case Failure<RuntimeValue> f -> Result.failure(f.diagnostics());
            case Success<RuntimeValue> s -> {
                environment.assign(assignment.name(), s.value());
                yield Result.success(statement);
            }
        };
    }
}