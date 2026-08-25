package printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.Statement;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
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
import printscript.semantic.GlobalSymbolTable;
import printscript.semantic.PrintScriptSemanticAnalyzer;

class PipelineIntegrationTest {

    private record RunResult(String output, boolean hadFailure) {}

    private RunResult run(String source) {
        var lexer = new PrintScriptLexer(new StringReader(source));
        var parser =
                new PrintScriptParser(
                        lexer,
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser());
        var semanticAnalyzer = new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable());

        var output = new ByteArrayOutputStream();
        var evaluator = new ExpressionEvaluator();
        var registry =
                new HandlerRegistry(
                        List.of(
                                new VariableDeclarationHandler(evaluator),
                                new AssignmentHandler(evaluator),
                                new PrintlnStatementHandler(evaluator, new PrintStream(output))));
        var interpreter =
                new PrintScriptInterpreter(semanticAnalyzer, new GlobalEnvironment(), registry);

        boolean hadFailure = false;
        while (interpreter.hasNext()) {
            Result<Statement> result = interpreter.next();
            hadFailure |=
                    switch (result) {
                        case Success<Statement> s -> false;
                        case Failure<Statement> f -> true;
                    };
        }

        return new RunResult(output.toString(StandardCharsets.UTF_8).strip(), hadFailure);
    }

    @Test
    void example1ConcatenatesStrings() {
        String source =
                """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """;

        RunResult result = run(source);

        assertEquals("Joe Doe", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void example2DividesNumbersAndConcatenatesResult() {
        String source =
                """
            let a: number = 12;
            let b: number = 4;
            let c: number = a / b;
            println("Result: " + c);
            """;

        RunResult result = run(source);

        assertEquals("Result: 3", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void example3ReassignsVariableAfterDivision() {
        String source =
                """
            let a: number = 12;
            let b: number = 4;
            a = a / b;
            println("Result: " + a);
            """;

        RunResult result = run(source);

        assertEquals("Result: 3", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void divisionByZeroStopsExecutionBeforePrinting() {
        // fix 8: la división por cero es un error de runtime que semantic NO detecta
        // (a y b son ambas number, tipos correctos). Antes el evaluator devolvía
        // ZERO en silencio y el println imprimía "0". Ahora falla, "c" nunca se
        // define en el environment, y el println que la usa también falla —
        // no se imprime nada.
        String source =
                """
            let a: number = 10;
            let b: number = 0;
            let c: number = a / b;
            println(c);
            """;

        RunResult result = run(source);

        assertTrue(result.hadFailure());
        assertEquals("", result.output());
    }
}
