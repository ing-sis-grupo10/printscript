package printscript.parser;

import printscript.ast.Statement;

public interface StatementParser {
    boolean canParse(TokenStream tokens);
    Statement parse(TokenStream tokens, ExpressionParser expressionParser);
}