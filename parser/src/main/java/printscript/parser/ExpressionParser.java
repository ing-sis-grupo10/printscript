package printscript.parser;

import printscript.ast.Expression;

public interface ExpressionParser {
    Expression parseExpression(TokenStream tokens);
}
