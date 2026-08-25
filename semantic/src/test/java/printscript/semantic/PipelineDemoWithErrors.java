package printscript.semantic;

import java.io.StringReader;
import java.util.List;
import printscript.ast.Statement;
import printscript.diagnostics.CollectingDiagnosticReporter;
import printscript.diagnostics.Diagnostic;
import printscript.lexer.PrintScriptLexer;
import printscript.parser.AssignmentParser;
import printscript.parser.PrecedenceClimbingExpressionParser;
import printscript.parser.PrintScriptParser;
import printscript.parser.PrintlnStatementParser;
import printscript.parser.VariableDeclarationParser;

public class PipelineDemoWithErrors {

    public static void main(String[] args) {
        // tres errores distintos, a propósito, para ver que se juntan TODOS, no solo el primero
        String source =
                """
            let x: number = "esto no es un numero";
            println(y);
            let x: number = 5;
            """;

        System.out.println("=== Código fuente ===");
        System.out.println(source);

        var reporter = new CollectingDiagnosticReporter();
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

        var analyzer = new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable(), reporter);

        System.out.println("=== Procesando statement por statement ===");
        int i = 1;
        while (analyzer.hasNext()) {
            Statement statement = analyzer.next();
            System.out.println("--- Statement " + i + " ---");
            System.out.println("Tipo de nodo: " + statement.getClass().getSimpleName());
            System.out.println("Contenido: " + statement);
            System.out.println(
                    "Diagnósticos acumulados hasta ahora: " + reporter.diagnostics().size());
            System.out.println();
            i++;
        }

        System.out.println("=== Resultado final ===");
        if (reporter.hasErrors()) {
            System.out.println("Se encontraron " + reporter.diagnostics().size() + " problema(s):");
            for (Diagnostic diagnostic : reporter.diagnostics()) {
                System.out.println(
                        "  ["
                                + diagnostic.severity()
                                + "] "
                                + diagnostic.message()
                                + " en "
                                + diagnostic.span());
            }
        } else {
            System.out.println(
                    "Programa válido — " + (i - 1) + " statements procesados sin errores.");
        }
    }
}
