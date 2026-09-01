package printscript.interpreter.handler;

import java.util.Optional;
import printscript.ast.Assignment;
import printscript.ast.DeclaredType;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
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
        Optional<DeclaredType> declaredType = environment.typeOf(assignment.name());
        if (declaredType.isEmpty()) {
            return Result.failure(
                    Diagnostic.error(
                            "Variable no declarada: " + assignment.name(), assignment.span()));
        }

        Result<RuntimeValue> value = evaluator.evaluate(assignment.value(), environment);
        if (value instanceof Failure<RuntimeValue> f) {
            return Result.failure(f.diagnostics());
        }
        RuntimeValue runtimeValue = ((Success<RuntimeValue>) value).value();

        if (evaluator.typeOf(runtimeValue) != declaredType.get()) {
            return Result.failure(
                    Diagnostic.error(
                            "No se puede asignar "
                                    + evaluator.typeOf(runtimeValue)
                                    + " a "
                                    + assignment.name()
                                    + " (declarada como "
                                    + declaredType.get()
                                    + ")",
                            assignment.value().span()));
        }

        environment.assign(assignment.name(), runtimeValue);
        return Result.success(statement);
    }
}
