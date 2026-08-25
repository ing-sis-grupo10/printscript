package printscript.interpreter.handler;

import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.diagnostics.DiagnosticReporter;
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
    public void handle(Statement statement, Environment environment, DiagnosticReporter reporter) {
        VariableDeclaration declaration = (VariableDeclaration) statement;
        declaration
                .initializer()
                .ifPresent(
                        initializer -> {
                            RuntimeValue value =
                                    evaluator.evaluate(initializer, environment, reporter);
                            environment.define(declaration.name(), value);
                        });
    }
}
