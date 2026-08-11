package parser;

import edu.austral.printscript.common.token.Span;
import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;

public final class PrintlnStatementParser implements StatementParser {

    @Override
    public boolean canParse(TokenStream tokens) {
        return tokens.peek().type() == TokenType.PRINTLN;
    }

    @Override
    public Statement parse(TokenStream tokens, ExpressionParser expressionParser, DiagnosticReporter reporter) {
        Token printlnToken = tokens.consume(); // PRINTLN
        tokens.expect(TokenType.LEFT_PAREN);
        var argument = expressionParser.parseExpression(tokens, reporter);
        tokens.expect(TokenType.RIGHT_PAREN);
        Token semicolon = tokens.expect(TokenType.SEMICOLON);

        return new PrintlnStatement(argument, Span.merge(printlnToken.span(), semicolon.span()));
    }
}