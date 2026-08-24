package printscript.interpreter.handler;

import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;
import printscript.interpreter.runtime.Environment;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.RuntimeValue;

import java.io.PrintStream;

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
    public void handle(Statement statement, Environment environment, DiagnosticReporter reporter) {
        PrintlnStatement println = (PrintlnStatement) statement;
        RuntimeValue value = evaluator.evaluate(println.argument(), environment, reporter);
        out.println(evaluator.display(value));
    }
}