package printscript.analyzer.rules;

import java.util.List;
import java.util.regex.Pattern;
import printscript.analyzer.AnalyzerRules.IdentifierCase;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.common.result.Diagnostic;

// Los identificadores declarados deben estar en camelCase o snake_case según la config.
public record IdentifierCaseRule(IdentifierCase expectedCase) implements LintRule {

    private static final Pattern CAMEL_CASE = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]*$");

    @Override
    public List<Diagnostic> check(Statement statement) {
        if (!(statement instanceof VariableDeclaration declaration)) {
            return List.of();
        }
        Pattern expected = expectedCase == IdentifierCase.SNAKE_CASE ? SNAKE_CASE : CAMEL_CASE;
        if (expected.matcher(declaration.name()).matches()) {
            return List.of();
        }
        return List.of(
                Diagnostic.warning(
                        "El identificador '" + declaration.name() + "' no respeta " + expectedCase,
                        declaration.span()));
    }
}
