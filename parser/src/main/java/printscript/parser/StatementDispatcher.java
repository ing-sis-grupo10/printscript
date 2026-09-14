package printscript.parser;

import java.util.List;
import printscript.ast.Statement;
import printscript.common.token.Token;

final class StatementDispatcher {

    private StatementDispatcher() {}

    static Statement dispatch(
            TokenStream tokens,
            List<StatementParser> statementParsers,
            ExpressionParser expressionParser) {
        for (StatementParser statementParser : statementParsers) {
            if (statementParser.canParse(tokens)) {
                return statementParser.parse(tokens, expressionParser);
            }
        }
        Token unexpected = tokens.consume();
        throw new ParseError("No se esperaba: " + unexpected.value(), unexpected.span());
    }
}
