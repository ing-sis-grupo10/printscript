package printscript.semantic;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.diagnostics.CollectingDiagnosticReporter;
import printscript.lexer.PrintScriptLexer;
import printscript.parser.AssignmentParser;
import printscript.parser.PrecedenceClimbingExpressionParser;
import printscript.parser.PrintScriptParser;
import printscript.parser.PrintlnStatementParser;
import printscript.parser.VariableDeclarationParser;

class PipelineIntegrationTest {

    private SemanticAnalyzer pipelineFor(String source, CollectingDiagnosticReporter reporter) {
        var lexer = new PrintScriptLexer(new StringReader(source));
        PrintScriptParser parser =
                new PrintScriptParser(
                        lexer,
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser(),
                        reporter);
        return new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable(), reporter);
    }

    @Test
    void validProgramProducesNoDiagnostics() {
        // el ejemplo 1 literal de la consigna
        String source =
                """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """;

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = pipelineFor(source, reporter);

        int statementCount = 0;
        while (analyzer.hasNext()) {
            analyzer.next();
            statementCount++;
        }

        assertEquals(3, statementCount);
        assertFalse(reporter.hasErrors());
    }

    @Test
    void typeMismatchIsCaughtEndToEnd() {
        String source = "let x: number = \"esto no es un numero\";";

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = pipelineFor(source, reporter);

        while (analyzer.hasNext()) {
            analyzer.next();
        }

        assertTrue(reporter.hasErrors());
    }

    @Test
    void undeclaredVariableIsCaughtEndToEnd() {
        String source = "println(nuncaDeclarada);";

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = pipelineFor(source, reporter);

        while (analyzer.hasNext()) {
            analyzer.next();
        }

        assertTrue(reporter.hasErrors());
    }

    @Test
    void syntaxErrorRecoversAndContinuesToNextStatement() {
        // falta el ':' en la primera línea
        String source = """
            let x number = 5;
            println(x);
            """;

        var reporter = new CollectingDiagnosticReporter();
        var analyzer = pipelineFor(source, reporter);

        int statementCount = 0;
        while (analyzer.hasNext()) {
            analyzer.next();
            statementCount++;
        }

        assertTrue(reporter.hasErrors());
        assertEquals(1, statementCount); // la línea rota se descarta entera, solo llega el println
    }
}
