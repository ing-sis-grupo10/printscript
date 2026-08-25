package printscript.interpreter.handler;

import java.io.PrintStream;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.runtime.Environment;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.RuntimeValue;

public final class PrintlnStatementHandler implements StatementHandler {
    private final ExpressionEvaluator evaluator;
    private final PrintStream out;

    public PrintlnStatementHandler(ExpressionEvaluator evaluator, PrintStream out) {
        this.evaluator = evaluator;
        this.out = out;
    }

    @Override
    public boolean canHandle(Statement statement) {
        return statement instanceof PrintlnStatement;
    }

    @Override
    public Result<Statement> handle(Statement statement, Environment environment) {
        PrintlnStatement println = (PrintlnStatement) statement;
        Result<RuntimeValue> value = evaluator.evaluate(println.argument(), environment);
        return switch (value) {
            case Failure<RuntimeValue> f -> Result.failure(f.diagnostics());
            case Success<RuntimeValue> s -> {
                out.println(evaluator.display(s.value()));
                yield Result.success(statement);
            }
        };
    }
}
