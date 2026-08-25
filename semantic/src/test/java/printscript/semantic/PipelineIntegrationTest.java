package printscript.semantic;

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
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineIntegrationTest {

    private PrintScriptSemanticAnalyzer pipelineFor(String source) {
        var lexer = new PrintScriptLexer(new StringReader(source));
        PrintScriptParser parser = new PrintScriptParser(lexer,
                List.of(new VariableDeclarationParser(), new AssignmentParser(), new PrintlnStatementParser()),
                new PrecedenceClimbingExpressionParser());
        return new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable());
    }

    private boolean isFailure(Result<Statement> result) {
        return switch (result) {
            case Success<Statement> s -> false;
            case Failure<Statement> f -> true;
        };
    }

    @Test
    void validProgramProducesNoDiagnostics() {
        String source = """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """;

        var analyzer = pipelineFor(source);

        int statementCount = 0;
        boolean anyFailure = false;
        while (analyzer.hasNext()) {
            anyFailure |= isFailure(analyzer.next());
            statementCount++;
        }

        assertEquals(3, statementCount);
        assertFalse(anyFailure);
    }

    @Test
    void typeMismatchIsCaughtEndToEnd() {
        String source = "let x: number = \"esto no es un numero\";";

        var analyzer = pipelineFor(source);

        boolean anyFailure = false;
        while (analyzer.hasNext()) {
            anyFailure |= isFailure(analyzer.next());
        }

        assertTrue(anyFailure);
    }

    @Test
    void undeclaredVariableIsCaughtEndToEnd() {
        String source = "println(nuncaDeclarada);";

        var analyzer = pipelineFor(source);

        boolean anyFailure = false;
        while (analyzer.hasNext()) {
            anyFailure |= isFailure(analyzer.next());
        }

        assertTrue(anyFailure);
    }

    @Test
    void syntaxErrorRecoversAndContinuesToNextStatement() {
        // falta el ':' en la primera línea
        String source = """
            let x number = 5;
            println(x);
            """;

        var analyzer = pipelineFor(source);

        int statementCount = 0;
        boolean anyFailure = false;
        while (analyzer.hasNext()) {
            anyFailure |= isFailure(analyzer.next());
            statementCount++;
        }

        assertTrue(anyFailure);
        // la línea rota ahora es su propio Failure (statement 1), y el println
        // que la sigue es un Success aparte (statement 2) — ya no se "esconden"
        // los errores recursando hasta el próximo éxito, cada next() cuenta.
        assertEquals(2, statementCount);
    }
}