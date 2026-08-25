package printscript.interpreter.handler;

import java.util.List;
import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;
import printscript.interpreter.runtime.Environment;

public final class HandlerRegistry {
    private final List<StatementHandler> handlers;

    public HandlerRegistry(List<StatementHandler> handlers) {
        this.handlers = handlers;
    }

    public void dispatch(
            Statement statement, Environment environment, DiagnosticReporter reporter) {
        for (StatementHandler handler : handlers) {
            if (handler.canHandle(statement)) {
                handler.handle(statement, environment, reporter);
                return;
            }
        }
        throw new IllegalStateException(
                "No hay handler para: " + statement.getClass().getSimpleName());
    }
}
