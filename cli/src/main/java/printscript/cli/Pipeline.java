package printscript.cli;

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

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class Pipeline {
    private final String sourceFile;
    private final String configFile;

    Pipeline(String sourceFile, String configFile) {
        this.sourceFile = sourceFile;
        this.configFile = configFile;
    }

    int validate() throws IOException {
        return report(drain(buildInterpreter(silentOutput())));
    }

    int execute() throws IOException {
        return report(drain(buildInterpreter(System.out)));
    }

    int format() throws IOException {
        FormattingRules rules = configFile != null
            ? new FormattingRulesLoader().load(new FileReader(configFile))
            : FormattingRules.defaults();
        try (var source = new FileReader(sourceFile)) {
            new PrintScriptFormatter(rules).format(source, new OutputStreamWriter(System.out));
        }
        return 0;
    }

    int analyze() throws IOException {
        AnalyzerRules rules = configFile != null
            ? new AnalyzerRulesLoader().load(new FileReader(configFile))
            : AnalyzerRules.defaults();
        var analyzer = new PrintScriptAnalyzer(buildInterpreter(silentOutput()), rules);
        List<Diagnostic> diagnostics = new ArrayList<>(drain(analyzer));
        diagnostics.addAll(analyzer.diagnostics());
        return report(diagnostics);
    }

    private PrintScriptInterpreter buildInterpreter(PrintStream out) throws IOException {
        var lexer = new PrintScriptLexer(new FileReader(sourceFile));
        var parser = new PrintScriptParser(
            lexer,
            List.of(new VariableDeclarationParser(), new AssignmentParser(), new PrintlnStatementParser()),
            new PrecedenceClimbingExpressionParser());

        var evaluator = new ExpressionEvaluator();
        var handlers = new HandlerRegistry(List.of(
            new VariableDeclarationHandler(evaluator),
            new AssignmentHandler(evaluator),
            new PrintlnStatementHandler(evaluator, out)
        ));
        Environment environment = new GlobalEnvironment();
        return new PrintScriptInterpreter(parser, environment, handlers);
    }

    private PrintStream silentOutput() {
        return new PrintStream(OutputStream.nullOutputStream());
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
            System.err.println("[" + d.severity() + "] " + d.message()
                + " (" + d.span().start().line() + ":" + d.span().start().column()
                + " - " + d.span().end().line() + ":" + d.span().end().column() + ")");
        }
        boolean hasErrors = diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR);
        return hasErrors ? 1 : 0;
    }
}
