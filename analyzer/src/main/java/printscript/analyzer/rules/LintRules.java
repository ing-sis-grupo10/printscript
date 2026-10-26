package printscript.analyzer.rules;

import java.util.ArrayList;
import java.util.List;
import printscript.analyzer.AnalyzerRules;

// Arma la lista de reglas activas a partir de la config leída del JSON.
public final class LintRules {

    private LintRules() {}

    public static List<LintRule> from(AnalyzerRules config) {
        List<LintRule> rules = new ArrayList<>();
        if (config.identifierCaseEnabled()) {
            rules.add(new IdentifierCaseRule(config.identifierCase()));
        }
        if (config.printlnOnlyIdentifierOrLiteral()) {
            rules.add(new PrintlnArgumentRule());
        }
        if (config.readInputOnlyIdentifierOrLiteral()) {
            rules.add(new ReadInputArgumentRule());
        }
        return List.copyOf(rules);
    }
}
