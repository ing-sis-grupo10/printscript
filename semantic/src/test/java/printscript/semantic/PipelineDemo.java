package printscript.semantic;

import edu.austral.printscript.lexer.PrintScriptLexer;
import parser.AssignmentParser;
import parser.PrecedenceClimbingExpressionParser;
import parser.PrintScriptParser;
import parser.PrintlnStatementParser;
import parser.VariableDeclarationParser;
import printscript.ast.Statement;
import printscript.diagnostics.CollectingDiagnosticReporter;
import printscript.diagnostics.Diagnostic;

import java.io.StringReader;
import java.util.List;

public class PipelineDemo {

    public static void main(String[] args) {
        String source = """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """;

        System.out.println("=== Código fuente ===");
        System.out.println(source);

        var reporter = new CollectingDiagnosticReporter();
        var lexer = new PrintScriptLexer(new StringReader(source));

        PrintScriptParser parser = new PrintScriptParser(lexer,
                List.of(new VariableDeclarationParser(), new AssignmentParser(), new PrintlnStatementParser()),
                new PrecedenceClimbingExpressionParser(), reporter);

        var analyzer = new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable(), reporter);

        System.out.println("=== Procesando statement por statement ===");
        int i = 1;
        while (analyzer.hasNext()) {
            Statement statement = analyzer.next();
            System.out.println("--- Statement " + i + " ---");
            System.out.println("Tipo de nodo: " + statement.getClass().getSimpleName());
            System.out.println("Contenido: " + statement);
            System.out.println("Diagnósticos acumulados hasta ahora: " + reporter.diagnostics().size());
            System.out.println();
            i++;
        }

        System.out.println("=== Resultado final ===");
        if (reporter.hasErrors()) {
            System.out.println("Se encontraron errores:");
            for (Diagnostic diagnostic : reporter.diagnostics()) {
                System.out.println("  [" + diagnostic.severity() + "] " + diagnostic.message() + " en " + diagnostic.span());
            }
        } else {
            System.out.println("Programa válido — " + (i - 1) + " statements procesados sin errores.");
        }
    }
}