package printscript.interpreter.handler;

import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;
import printscript.interpreter.runtime.Environment;

public interface StatementHandler {
    boolean canHandle(Statement statement);
    void handle(Statement statement, Environment environment, DiagnosticReporter reporter);
}