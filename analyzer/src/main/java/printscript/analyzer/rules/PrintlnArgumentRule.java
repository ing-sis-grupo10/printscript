package printscript.analyzer.rules;

import java.util.List;
import printscript.ast.Expression;
import printscript.ast.PrintlnStatement;
import printscript.ast.ReadInputExpression;
import printscript.ast.Statement;
import printscript.common.result.Diagnostic;

// println solo puede recibir un identificador o un literal (o un readInput), no una expresión.
public final class PrintlnArgumentRule implements LintRule {

    @Override
    public List<Diagnostic> check(Statement statement) {
        if (!(statement instanceof PrintlnStatement println)) {
            return List.of();
        }
        Expression argument = println.argument();
        if (Arguments.isIdentifierOrLiteral(argument) || argument instanceof ReadInputExpression) {
            return List.of();
        }
        return List.of(
                Diagnostic.warning(
                        "println solo puede recibir un identificador o un literal, no una expresión",
                        argument.span()));
    }
}
