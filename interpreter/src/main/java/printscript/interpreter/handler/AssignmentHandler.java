package printscript.interpreter.handler;

import printscript.ast.Assignment;
import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;
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
    public void handle(Statement statement, Environment environment, DiagnosticReporter reporter) {
        Assignment assignment = (Assignment) statement;
        RuntimeValue value = evaluator.evaluate(assignment.value(), environment, reporter);
        environment.assign(assignment.name(), value);
    }
}