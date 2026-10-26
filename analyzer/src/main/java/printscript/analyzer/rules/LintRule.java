package printscript.analyzer.rules;

import java.util.List;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;

// Una regla de linteo: mira una sentencia y devuelve los warnings que encuentre (o ninguno).
@FunctionalInterface
public interface LintRule {
    List<Diagnostic> check(Statement statement);
}
