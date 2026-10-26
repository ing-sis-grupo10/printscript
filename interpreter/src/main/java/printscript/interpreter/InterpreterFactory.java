package printscript.interpreter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import printscript.ast.Statement;
import printscript.common.Version;
import printscript.common.result.Result;
import printscript.interpreter.handler.AssignmentHandler;
import printscript.interpreter.handler.HandlerRegistry;
import printscript.interpreter.handler.IfStatementHandler;
import printscript.interpreter.handler.PrintlnStatementHandler;
import printscript.interpreter.handler.StatementHandler;
import printscript.interpreter.handler.VariableDeclarationHandler;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.GlobalEnvironment;
import printscript.interpreter.runtime.InputSource;

// Único lugar que sabe qué sentencias se pueden ejecutar en cada versión del lenguaje.
public final class InterpreterFactory {

    private InterpreterFactory() {}

    public static PrintScriptInterpreter create(
            Iterator<Result<Statement>> statements,
            Version version,
            PrintStream out,
            InputSource input) {
        var evaluator = new ExpressionEvaluator(input);
        List<StatementHandler> handlers = new ArrayList<>();
        HandlerRegistry registry = new HandlerRegistry(handlers);

        handlers.add(new VariableDeclarationHandler(evaluator));
        handlers.add(new AssignmentHandler(evaluator));
        handlers.add(new PrintlnStatementHandler(evaluator, out));

        List<StatementHandler> extras =
                switch (version) {
                    case V1_0 -> List.of();
                    case V1_1 -> List.of(new IfStatementHandler(evaluator, () -> registry));
                };
        handlers.addAll(extras);

        return new PrintScriptInterpreter(statements, new GlobalEnvironment(), registry);
    }
}
