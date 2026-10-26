package printscript.analyzer.rules;

import printscript.ast.Expression;
import printscript.ast.Identifier;
import printscript.ast.NumberLiteral;
import printscript.ast.StringLiteral;

final class Arguments {

    private Arguments() {}

    static boolean isIdentifierOrLiteral(Expression expression) {
        return expression instanceof Identifier
                || expression instanceof NumberLiteral
                || expression instanceof StringLiteral;
    }
}
