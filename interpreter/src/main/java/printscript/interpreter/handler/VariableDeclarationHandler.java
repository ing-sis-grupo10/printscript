package printscript.interpreter.handler;

import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.runtime.Environment;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.RuntimeValue;

public final class VariableDeclarationHandler implements StatementHandler {
    private final ExpressionEvaluator evaluator;

    public VariableDeclarationHandler(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public boolean canHandle(Statement statement) {
        return statement instanceof VariableDeclaration;
    }

    @Override
    public Result<Statement> handle(Statement statement, Environment environment) {
        VariableDeclaration declaration = (VariableDeclaration) statement;
        if (declaration.initializer().isEmpty()) {
            return Result.success(statement);
        }

        Result<RuntimeValue> value =
                evaluator.evaluate(declaration.initializer().get(), environment);
        return switch (value) {
            case Failure<RuntimeValue> f -> Result.failure(f.diagnostics());
            case Success<RuntimeValue> s -> {
                environment.define(declaration.name(), s.value());
                yield Result.success(statement);
            }
        };
    }
}
