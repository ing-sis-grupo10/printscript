package printscript.interpreter.handler;

import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Result;
import printscript.interpreter.runtime.Environment;

import java.util.List;

public final class HandlerRegistry {
    private final List<StatementHandler> handlers;

    public HandlerRegistry(List<StatementHandler> handlers) {
        this.handlers = handlers;
    }

    public Result<Statement> dispatch(Statement statement, Environment environment) {
        for (StatementHandler handler : handlers) {
            if (handler.canHandle(statement)) {
                return handler.handle(statement, environment);
            }
        }
        return Result.failure(Diagnostic.error(
                "No hay handler para: " + statement.getClass().getSimpleName(), statement.span()));
    }
}