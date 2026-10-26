package printscript.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import printscript.analyzer.rules.LintRule;
import printscript.analyzer.rules.LintRules;
import printscript.ast.IfStatement;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Result;
import printscript.common.result.Success;

public final class PrintScriptAnalyzer implements Analyzer {
    private final Iterator<Result<Statement>> statements;
    private final List<LintRule> rules;
    private final List<Diagnostic> collected = new ArrayList<>();

    public PrintScriptAnalyzer(Iterator<Result<Statement>> statements, AnalyzerRules config) {
        this(statements, LintRules.from(config));
    }

    public PrintScriptAnalyzer(Iterator<Result<Statement>> statements, List<LintRule> rules) {
        this.statements = statements;
        this.rules = List.copyOf(rules);
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Result<Statement> next() {
        Result<Statement> upstream = statements.next();
        if (upstream instanceof Success<Statement> success) {
            analyze(success.value());
        }
        return upstream;
    }

    public List<Diagnostic> diagnostics() {
        return List.copyOf(collected);
    }

    // El recorrido vive acá: cada regla ve cada sentencia, incluidas las de adentro de los if.
    // Las reglas no saben que existen los bloques.
    private void analyze(Statement statement) {
        for (LintRule rule : rules) {
            collected.addAll(rule.check(statement));
        }
        if (statement instanceof IfStatement ifStatement) {
            ifStatement.thenBranch().forEach(this::analyze);
            ifStatement.elseBranch().ifPresent(branch -> branch.forEach(this::analyze));
        }
    }
}
