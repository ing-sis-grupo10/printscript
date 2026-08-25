package printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.diagnostics.CollectingDiagnosticReporter;
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

    private String run(String source, CollectingDiagnosticReporter reporter) {
        var lexer = new PrintScriptLexer(new StringReader(source));
        var parser =
                new PrintScriptParser(
                        lexer,
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);
        var semanticAnalyzer =
                new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable(), reporter);

        var output = new ByteArrayOutputStream();
        var evaluator = new ExpressionEvaluator();
        var registry =
                new HandlerRegistry(
                        List.of(
                                new VariableDeclarationHandler(evaluator),
                                new AssignmentHandler(evaluator),
                                new PrintlnStatementHandler(evaluator, new PrintStream(output))));
        Interpreter interpreter =
                new PrintScriptInterpreter(
                        semanticAnalyzer, new GlobalEnvironment(), reporter, registry);

        while (interpreter.hasNext()) {
            interpreter.next();
        }

        return output.toString(StandardCharsets.UTF_8).strip();
    }

    @Test
    void example1ConcatenatesStrings() {
        String source =
                """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """;

        var reporter = new CollectingDiagnosticReporter();
        String output = run(source, reporter);

        assertEquals("Joe Doe", output);
        assertFalse(reporter.hasErrors());
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

        var reporter = new CollectingDiagnosticReporter();
        String output = run(source, reporter);

        assertEquals("Result: 3", output);
        assertFalse(reporter.hasErrors());
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

        var reporter = new CollectingDiagnosticReporter();
        String output = run(source, reporter);

        assertEquals("Result: 3", output);
        assertFalse(reporter.hasErrors());
    }
}
