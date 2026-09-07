package printscript.analyzer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.PrintScriptInterpreter;
import printscript.interpreter.handler.AssignmentHandler;
import printscript.interpreter.handler.HandlerRegistry;
import printscript.interpreter.handler.PrintlnStatementHandler;
import printscript.interpreter.handler.VariableDeclarationHandler;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.GlobalEnvironment;
import printscript.lexer.PrintScriptLexer;
import printscript.parser.AssignmentParser;
import printscript.parser.PrecedenceClimbingExpressionParser;
import printscript.parser.PrintScriptParser;
import printscript.parser.PrintlnStatementParser;
import printscript.parser.VariableDeclarationParser;

public class AnalyzerPipelineDemoWithWarnings {

    public static void main(String[] args) {
        // dos violaciones de convención (no bloquean nada) + un error semántico real (sí bloquea)
        String source =
                """
        let My_Name: string = "Joe";
        println(1 + 2);
        println(undeclaredVar);
        """;

        System.out.println("=== Código fuente ===");
        System.out.println(source);

        var analyzer = buildAnalyzer(source);

        System.out.println("=== Procesando statement por statement ===");
        int i = 1;
        while (analyzer.hasNext()) {
            printResult(i, analyzer.next());
            i++;
        }

        System.out.println("=== Warnings de convención acumulados por el analyzer ===");
        analyzer.diagnostics().forEach(AnalyzerPipelineDemoWithWarnings::printDiagnostic);
    }

    private static PrintScriptAnalyzer buildAnalyzer(String source) {
        var lexer = new PrintScriptLexer(new StringReader(source));
        var parser =
                new PrintScriptParser(
                        lexer,
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser());
        var evaluator = new ExpressionEvaluator();
        var handlers =
                new HandlerRegistry(
                        List.of(
                                new VariableDeclarationHandler(evaluator),
                                new AssignmentHandler(evaluator),
                                new PrintlnStatementHandler(
                                        evaluator,
                                        new PrintStream(OutputStream.nullOutputStream()))));
        var interpreter = new PrintScriptInterpreter(parser, new GlobalEnvironment(), handlers);
        return new PrintScriptAnalyzer(interpreter, AnalyzerRules.defaults());
    }

    private static void printResult(int index, Result<Statement> result) {
        System.out.println("--- Statement " + index + " ---");
        switch (result) {
            case Success<Statement> s ->
                    System.out.println("OK (sigue siendo válido): " + s.value());
            case Failure<Statement> f ->
                    f.diagnostics().forEach(AnalyzerPipelineDemoWithWarnings::printDiagnostic);
        }
        System.out.println();
    }

    private static void printDiagnostic(Diagnostic diagnostic) {
        System.out.println(
                "  ["
                        + diagnostic.severity()
                        + "] "
                        + diagnostic.message()
                        + " en "
                        + diagnostic.span());
    }
}
