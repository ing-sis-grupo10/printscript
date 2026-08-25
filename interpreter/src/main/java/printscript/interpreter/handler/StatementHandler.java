package printscript.interpreter.handler;

import printscript.ast.Statement;
import printscript.common.result.Result;
import printscript.interpreter.runtime.Environment;

public interface StatementHandler {
    boolean canHandle(Statement statement);
    Result<Statement> handle(Statement statement, Environment environment);
}