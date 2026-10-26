package printscript.cli;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import printscript.common.Version;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Severity;
import printscript.common.token.Span;
import printscript.formatter.FormattingRules;
import printscript.formatter.FormattingRulesLoader;
import printscript.formatter.PrintScriptFormatter;
import printscript.interpreter.InterpreterFactory;
import printscript.interpreter.PrintScriptInterpreter;
import printscript.interpreter.runtime.ConsoleInputSource;
import printscript.interpreter.runtime.InputSource;
import printscript.lexer.LexerFactory;
import printscript.parser.ParserFactory;

final class Pipeline {
    private final String sourceFile;
    private final String configFile;
    private final Version version;

    Pipeline(String sourceFile, String configFile) {
        this(sourceFile, configFile, Version.latest());
    }

    Pipeline(String sourceFile, String configFile, Version version) {
        this.sourceFile = sourceFile;
        this.configFile = configFile;
        this.version = version;
    }

    int validate() throws IOException {
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            return report(drain(buildInterpreter(source, silentOutput(), nonInteractiveInput())));
        }
    }

    int execute() throws IOException {
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            InputSource inputSource =
                    new ConsoleInputSource(
                            new InputStreamReader(System.in, StandardCharsets.UTF_8), System.out);
            return report(drain(buildInterpreter(source, System.out, inputSource)));
        }
    }

    int format() throws IOException {
        FormattingRules rules = loadFormattingRules();
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            var writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
            new PrintScriptFormatter(rules).format(source, writer, version);
            writer.flush();
        }
        return 0;
    }

    int analyze() throws IOException {
        AnalyzerRules rules = loadAnalyzerRules();
        try (var source = new FileReader(sourceFile, StandardCharsets.UTF_8)) {
            var analyzer =
                    new PrintScriptAnalyzer(
                            buildInterpreter(source, silentOutput(), nonInteractiveInput()), rules);
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

    private PrintScriptInterpreter buildInterpreter(
            Reader source, PrintStream out, InputSource inputSource) {
        var tokens = LexerFactory.create(source, version);
        var statements = ParserFactory.create(tokens, version);
        return InterpreterFactory.create(statements, version, out, inputSource);
    }

    private PrintStream silentOutput() {
        return new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8);
    }

    private InputSource nonInteractiveInput() {
        return prompt -> prompt;
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
                    "[" + d.severity() + "] " + d.message() + " (" + describe(d.span()) + ")");
        }
        boolean hasErrors = diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR);
        return hasErrors ? 1 : 0;
    }

    // Internamente las posiciones empiezan en 0 y la columna de fin es exclusiva.
    // Al usuario se las mostramos como un editor: desde 1 y con el fin inclusivo.
    static String describe(Span span) {
        int startLine = span.start().line() + 1;
        int startColumn = span.start().column() + 1;
        int endLine = span.end().line() + 1;
        int endColumn = span.end().equals(span.start()) ? startColumn : span.end().column();
        return startLine + ":" + startColumn + " - " + endLine + ":" + endColumn;
    }
}
