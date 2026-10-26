package printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import printscript.analyzer.rules.LintRule;
import printscript.analyzer.rules.LintRules;
import printscript.ast.Identifier;
import printscript.ast.IfStatement;
import printscript.ast.NumberLiteral;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Result;
import printscript.common.token.Position;
import printscript.common.token.Span;

class CustomLintRulesTest {

    private final Span span = Span.of(new Position(0, 0), new Position(0, 1));

    private List<Diagnostic> analyze(List<Statement> statements, List<LintRule> rules) {
        List<Result<Statement>> wrapped =
                statements.stream().map(Result::<Statement>success).toList();
        var analyzer = new PrintScriptAnalyzer(wrapped.iterator(), rules);
        while (analyzer.hasNext()) {
            analyzer.next();
        }
        return analyzer.diagnostics();
    }

    @Test
    void injectedRuleSeesStatementsInsideIfBranches() {
        LintRule warnOnEveryPrintln =
                statement ->
                        statement instanceof PrintlnStatement
                                ? List.of(Diagnostic.warning("println", statement.span()))
                                : List.of();
        Statement println = new PrintlnStatement(new NumberLiteral(BigDecimal.ONE, span), span);
        Statement ifStatement =
                new IfStatement(
                        new Identifier("flag", span),
                        List.of(println),
                        Optional.of(List.of(println)),
                        span);

        assertEquals(2, analyze(List.of(ifStatement), List.of(warnOnEveryPrintln)).size());
    }

    @Test
    void disabledRulesAreNotInTheList() {
        var allOff =
                new AnalyzerRules(AnalyzerRules.IdentifierCase.CAMEL_CASE, false, false, false);

        assertTrue(LintRules.from(allOff).isEmpty());
        assertEquals(3, LintRules.from(AnalyzerRules.defaults()).size());
    }
}
