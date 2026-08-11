package parser;

import edu.austral.printscript.common.token.Span;
import edu.austral.printscript.common.token.Token;
import edu.austral.printscript.common.token.TokenType;
import printscript.ast.Assignment;
import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;

public final class AssignmentParser implements StatementParser {

    @Override
    public boolean canParse(TokenStream tokens) {
        return tokens.peek().type() == TokenType.IDENTIFIER;
    }

    @Override
    public Statement parse(TokenStream tokens, ExpressionParser expressionParser, DiagnosticReporter reporter) {
        Token nameToken = tokens.consume(); // IDENTIFIER
        tokens.expect(TokenType.ASSIGN);
        var value = expressionParser.parseExpression(tokens, reporter);
        Token semicolon = tokens.expect(TokenType.SEMICOLON);

        return new Assignment(nameToken.value(), value, Span.merge(nameToken.span(), semicolon.span()));
    }
}