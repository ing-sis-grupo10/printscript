package printscript.semantic;

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

        var lexer = new PrintScriptLexer(new StringReader(source));
        PrintScriptParser parser = new PrintScriptParser(lexer,
                List.of(new VariableDeclarationParser(), new AssignmentParser(), new PrintlnStatementParser()),
                new PrecedenceClimbingExpressionParser());
        var analyzer = new PrintScriptSemanticAnalyzer(parser, new GlobalSymbolTable());

        System.out.println("=== Procesando statement por statement ===");
        int i = 1;
        boolean anyError = false;
        while (analyzer.hasNext()) {
            Result<Statement> result = analyzer.next();
            System.out.println("--- Statement " + i + " ---");
            switch (result) {
                case Success<Statement> s -> System.out.println("OK: " + s.value());
                case Failure<Statement> f -> {
                    anyError = true;
                    for (Diagnostic diagnostic : f.diagnostics()) {
                        System.out.println("  [" + diagnostic.severity() + "] " + diagnostic.message() + " en " + diagnostic.span());
                    }
                }
            }
            System.out.println();
            i++;
        }

        System.out.println("=== Resultado final ===");
        System.out.println(anyError
                ? "Se encontraron errores."
                : "Programa válido — " + (i - 1) + " statements procesados sin errores.");
    }
}