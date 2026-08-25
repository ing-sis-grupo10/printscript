package printscript.parser;

import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

public final class PrintlnStatementParser implements StatementParser {

    @Override
    public boolean canParse(TokenStream tokens) {
        return tokens.peek().type() == TokenType.PRINTLN;
    }

    @Override
    public Statement parse(TokenStream tokens, ExpressionParser expressionParser) {
        Token printlnToken = tokens.consume();
        tokens.expect(TokenType.LEFT_PAREN);
        var argument = expressionParser.parseExpression(tokens);
        tokens.expect(TokenType.RIGHT_PAREN);
        Token semicolon = tokens.expect(TokenType.SEMICOLON);

        return new PrintlnStatement(argument, Span.merge(printlnToken.span(), semicolon.span()));
    }
}
