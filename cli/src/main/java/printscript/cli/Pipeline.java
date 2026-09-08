package printscript.cli;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import printscript.analyzer.AnalyzerRules;
import printscript.analyzer.AnalyzerRulesLoader;
import printscript.analyzer.PrintScriptAnalyzer;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Severity;
import printscript.formatter.FormattingRules;
import printscript.formatter.FormattingRulesLoader;
import printscript.formatter.PrintScriptFormatter;
import printscript.interpreter.PrintScriptInterpreter;
import printscript.interpreter.handler.AssignmentHandler;
import printscript.interpreter.handler.HandlerRegistry;
import printscript.interpreter.handler.PrintlnStatementHandler;
import printscript.interpreter.handler.VariableDeclarationHandler;
import printscript.interpreter.runtime.Environment;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.GlobalEnvironment;
import printscript.lexer.PrintScriptLexer;
import printscript.parser.AssignmentParser;
import printscript.parser.PrecedenceClimbingExpressionParser;
import printscript.parser.PrintScriptParser;
import printscript.parser.PrintlnStatementParser;
import printscript.parser.VariableDeclarationParser;

final class Pipeline {
    private final String sourceFile;
    private final String configFile;

    Pipeline(String sourceFile, String configFile) {
        this.sourceFile = sourceFile;
        this.configFile = configFile;
    }

    int validate() throws IOException {
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            return report(drain(buildInterpreter(source, silentOutput())));
        }
    }

    int execute() throws IOException {
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            return report(drain(buildInterpreter(source, System.out)));
        }
    }

    int format() throws IOException {
        FormattingRules rules = loadFormattingRules();
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            var writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
            new PrintScriptFormatter(rules).format(source, writer);
            writer.flush();
        }
        return 0;
    }

    int analyze() throws IOException {
        AnalyzerRules rules = loadAnalyzerRules();
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            var analyzer = new PrintScriptAnalyzer(buildInterpreter(source, silentOutput()), rules);
            List<Diagnostic> diagnostics = new ArrayList<>(drain(analyzer));
            diagnostics.addAll(analyzer.diagnostics());
            return report(diagnostics);
        }
    }

    private FormattingRules loadFormattingRules() throws IOException {
        if (configFile == null) {
            return FormattingRules.defaults();
        }
        try (var configReader = new FileReader(configFile, StandardCharsets.UTF_8)) {
            return new FormattingRulesLoader().load(configReader);
        }
    }

    private AnalyzerRules loadAnalyzerRules() throws IOException {
        if (configFile == null) {
            return AnalyzerRules.defaults();
        }
        try (var configReader = new FileReader(configFile, StandardCharsets.UTF_8)) {
            return new AnalyzerRulesLoader().load(configReader);
        }
    }

    private PrintScriptInterpreter buildInterpreter(Reader source, PrintStream out) {
        var lexer = new PrintScriptLexer(source);
        var parser =
                new PrintScriptParser(
                        lexer,
                        List.of(
                                new VariableDeclarationParser(),
                                new AssignmentParser(),
                                new PrintlnStatementParser()),
                        new PrecedenceClimbingExpressionParser());

        var evaluator = new ExpressionEvaluator();
        var handlers =
                new HandlerRegistry(
                        List.of(
                                new VariableDeclarationHandler(evaluator),
                                new AssignmentHandler(evaluator),
                                new PrintlnStatementHandler(evaluator, out)));
        Environment environment = new GlobalEnvironment();
        return new PrintScriptInterpreter(parser, environment, handlers);
    }

    private PrintStream silentOutput() {
        return new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8);
    }

    private List<Diagnostic> drain(Iterator<Result<Statement>> statements) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        int count = 0;
        while (statements.hasNext()) {
            Result<Statement> result = statements.next();
            if (result instanceof Failure<Statement> failure) {
                diagnostics.addAll(failure.diagnostics());
            }
            System.err.print("\rProcesando sentencia " + (++count) + "...");
        }
        System.err.println();
        return diagnostics;
    }

    private int report(List<Diagnostic> diagnostics) {
        for (Diagnostic d : diagnostics) {
            System.err.println(
                    "["
                            + d.severity()
                            + "] "
                            + d.message()
                            + " ("
                            + d.span().start().line()
                            + ":"
                            + d.span().start().column()
                            + " - "
                            + d.span().end().line()
                            + ":"
                            + d.span().end().column()
                            + ")");
        }
        boolean hasErrors = diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR);
        return hasErrors ? 1 : 0;
    }
}
