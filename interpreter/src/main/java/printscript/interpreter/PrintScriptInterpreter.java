package printscript.interpreter;

import printscript.ast.Statement;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.handler.HandlerRegistry;
import printscript.interpreter.runtime.Environment;

import java.util.Iterator;

public final class PrintScriptInterpreter implements Iterator<Result<Statement>> {
    private final Iterator<Result<Statement>> statements;
    private final Environment environment;
    private final HandlerRegistry handlers;

    public PrintScriptInterpreter(Iterator<Result<Statement>> statements, Environment environment,
                                  HandlerRegistry handlers) {
        this.statements = statements;
        this.environment = environment;
        this.handlers = handlers;
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Result<Statement> next() {
        Result<Statement> upstream = statements.next();
        return switch (upstream) {
            case Failure<Statement> f -> upstream;
            case Success<Statement> s -> handlers.dispatch(s.value(), environment);
        };
    }
}