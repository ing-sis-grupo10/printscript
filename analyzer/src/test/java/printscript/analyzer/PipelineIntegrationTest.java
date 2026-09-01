package printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.Statement;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.lexer.PrintScriptLexer;
import printscript.parser.AssignmentParser;
import printscript.parser.PrecedenceClimbingExpressionParser;
import printscript.parser.PrintScriptParser;
import printscript.parser.PrintlnStatementParser;
import printscript.parser.VariableDeclarationParser;
import printscript.semantic.GlobalSymbolTable;
import printscript.semantic.PrintScriptSemanticAnalyzer;

class PipelineIntegrationTest {

    private PrintScriptAnalyzer pipelineFor(String source, AnalyzerRules rules) {
        var lexer = new PrintScriptLexer(new StringReader(source));
        var parser =
                new PrintScriptParser(
                        lexer,
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser());
        var semantic = new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable());
        return new PrintScriptAnalyzer(semantic, rules);
    }

    private boolean isFailure(Result<Statement> result) {
        return switch (result) {
            case Success<Statement> s -> false;
            case Failure<Statement> f -> true;
        };
    }

    @Test
    void wellNamedProgramProducesNoWarnings() {
        String source =
                """
        let myName: string = "Joe";
        println(myName);
        """;

        var analyzer = pipelineFor(source, AnalyzerRules.defaults());

        while (analyzer.hasNext()) {
            assertFalse(isFailure(analyzer.next()));
        }
        assertTrue(analyzer.diagnostics().isEmpty());
    }

    @Test
    void badlyNamedVariableProducesWarningButStillSucceeds() {
        String source = "let My_Name: string = \"Joe\";";

        var analyzer = pipelineFor(source, AnalyzerRules.defaults());

        assertFalse(isFailure(analyzer.next()));
        assertEquals(1, analyzer.diagnostics().size());
    }

    @Test
    void semanticErrorIsNotHiddenByAnalyzer() {
        String source = "println(nuncaDeclarada);";

        var analyzer = pipelineFor(source, AnalyzerRules.defaults());

        assertTrue(isFailure(analyzer.next()));
        assertTrue(analyzer.diagnostics().isEmpty()); // el analyzer ni llegó a mirar esta sentencia
    }
}
