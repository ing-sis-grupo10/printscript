package printscript.analyzer;

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
import printscript.semantic.GlobalSymbolTable;
import printscript.semantic.PrintScriptSemanticAnalyzer;

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
        var analyzer = new PrintScriptAnalyzer(semantic, AnalyzerRules.defaults());

        System.out.println("=== Procesando statement por statement ===");
        int i = 1;
        while (analyzer.hasNext()) {
            Result<Statement> result = analyzer.next();
            System.out.println("--- Statement " + i + " ---");
            switch (result) {
                case Success<Statement> s -> System.out.println("OK (sigue siendo válido): " + s.value());
                case Failure<Statement> f -> {
                    for (Diagnostic diagnostic : f.diagnostics()) {
                        System.out.println(
                            "  [" + diagnostic.severity() + "] " + diagnostic.message()
                                + " en " + diagnostic.span());
                    }
                }
            }
            System.out.println();
            i++;
        }

        System.out.println("=== Warnings de convención acumulados por el analyzer ===");
        for (Diagnostic diagnostic : analyzer.diagnostics()) {
            System.out.println(
                "  [" + diagnostic.severity() + "] " + diagnostic.message()
                    + " en " + diagnostic.span());
        }
    }
}
