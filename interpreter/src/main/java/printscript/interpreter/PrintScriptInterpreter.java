package printscript.interpreter;

import java.util.Iterator;
import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;
import printscript.interpreter.handler.HandlerRegistry;
import printscript.interpreter.runtime.Environment;

public final class PrintScriptInterpreter implements Interpreter {
    private final Iterator<Statement> statements;
    private final Environment environment;
    private final DiagnosticReporter reporter;
    private final HandlerRegistry handlers;

    public PrintScriptInterpreter(
            Iterator<Statement> statements,
            Environment environment,
            DiagnosticReporter reporter,
            HandlerRegistry handlers) {
        this.statements = statements;
        this.environment = environment;
        this.reporter = reporter;
        this.handlers = handlers;
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Statement next() {
        Statement statement = statements.next();
        handlers.dispatch(statement, environment, reporter);
        return statement;
    }
}
