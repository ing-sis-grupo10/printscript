package printscript.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import printscript.ast.*;
import printscript.common.result.Diagnostic;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Span;

public final class PrintScriptAnalyzer implements Analyzer {
    private static final Pattern CAMEL_CASE = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final Iterator<Result<Statement>> statements;
    private final AnalyzerRules rules;
    private final List<Diagnostic> collected = new ArrayList<>();

    public PrintScriptAnalyzer(Iterator<Result<Statement>> statements, AnalyzerRules rules) {
        this.statements = statements;
        this.rules = rules;
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Result<Statement> next() {
        Result<Statement> upstream = statements.next();
        if (upstream instanceof Success<Statement> success) {
            check(success.value());
        }
        return upstream;
    }

    public List<Diagnostic> diagnostics() {
        return List.copyOf(collected);
    }

    private void check(Statement statement) {
        switch (statement) {
            case VariableDeclaration declaration ->
                    checkIdentifier(declaration.name(), declaration.span());
            case PrintlnStatement println -> checkPrintlnArgument(println.argument());
            case Assignment assignment -> {}
        }
    }

    private void checkIdentifier(String name, Span span) {
        Pattern expected =
                rules.identifierCase() == AnalyzerRules.IdentifierCase.SNAKE_CASE
                        ? SNAKE_CASE
                        : CAMEL_CASE;
        if (!expected.matcher(name).matches()) {
            collected.add(
                    Diagnostic.warning(
                            "El identificador '" + name + "' no respeta " + rules.identifierCase(),
                            span));
        }
    }

    private void checkPrintlnArgument(Expression argument) {
        if (!rules.printlnOnlyIdentifierOrLiteral()) return;
        boolean valid =
                argument instanceof Identifier
                        || argument instanceof NumberLiteral
                        || argument instanceof StringLiteral;
        if (!valid) {
            collected.add(
                    Diagnostic.warning(
                            "println solo puede recibir un identificador o un literal, no una expresión",
                            argument.span()));
        }
    }
}
