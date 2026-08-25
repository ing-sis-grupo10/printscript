package printscript.semantic;

import java.io.StringReader;
import java.util.List;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
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

        var lexer = new PrintScriptLexer(new StringReader(source));
        PrintScriptParser parser =
                new PrintScriptParser(
                        lexer,
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser());
        var analyzer = new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable());

        System.out.println("=== Procesando statement por statement ===");
        int i = 1;
        int errorCount = 0;
        while (analyzer.hasNext()) {
            Result<Statement> result = analyzer.next();
            System.out.println("--- Statement " + i + " ---");
            switch (result) {
                case Success<Statement> s -> System.out.println("OK: " + s.value());
                case Failure<Statement> f -> {
                    for (Diagnostic diagnostic : f.diagnostics()) {
                        errorCount++;
                        System.out.println(
                                "  ["
                                        + diagnostic.severity()
                                        + "] "
                                        + diagnostic.message()
                                        + " en "
                                        + diagnostic.span());
                    }
                }
            }
            System.out.println();
            i++;
        }

        System.out.println("=== Resultado final ===");
        System.out.println(
                errorCount == 0
                        ? "Programa válido — " + (i - 1) + " statements procesados sin errores."
                        : "Se encontraron " + errorCount + " problema(s).");
    }
}
