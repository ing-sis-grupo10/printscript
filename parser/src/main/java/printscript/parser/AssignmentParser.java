package printscript.parser;

import printscript.ast.Assignment;
import printscript.ast.Statement;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

public final class AssignmentParser implements StatementParser {

    @Override
    public boolean canParse(TokenStream tokens) {
        return tokens.peek().type() == TokenType.IDENTIFIER;
    }

    @Override
    public Statement parse(TokenStream tokens, ExpressionParser expressionParser) {
        Token nameToken = tokens.consume();
        tokens.expect(TokenType.ASSIGN);
        var value = expressionParser.parseExpression(tokens);
        Token semicolon = tokens.expect(TokenType.SEMICOLON);

        return new Assignment(
                nameToken.value(), value, Span.merge(nameToken.span(), semicolon.span()));
    }
}
