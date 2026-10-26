package printscript.analyzer.rules;

import java.util.List;
import java.util.Optional;
import printscript.ast.Assignment;
import printscript.ast.Expression;
import printscript.ast.IfStatement;
import printscript.ast.PrintlnStatement;
import printscript.ast.ReadInputExpression;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.common.result.Diagnostic;

// readInput solo puede recibir un identificador o un literal como mensaje, no una expresión.
public final class ReadInputArgumentRule implements LintRule {

    @Override
    public List<Diagnostic> check(Statement statement) {
        Optional<Expression> value = valueOf(statement);
        if (value.isEmpty() || !(value.get() instanceof ReadInputExpression readInput)) {
            return List.of();
        }
        Expression message = readInput.message();
        if (Arguments.isIdentifierOrLiteral(message)) {
            return List.of();
        }
        return List.of(
                Diagnostic.warning(
                        "readInput solo puede recibir un identificador o un literal, no una expresión",
                        message.span()));
    }

    // La expresión "principal" de cada sentencia, donde puede aparecer un readInput.
    private Optional<Expression> valueOf(Statement statement) {
        return switch (statement) {
            case VariableDeclaration declaration -> declaration.initializer();
            case PrintlnStatement println -> Optional.of(println.argument());
            case Assignment assignment -> Optional.of(assignment.value());
            case IfStatement ifStatement -> Optional.empty();
        };
    }
}
