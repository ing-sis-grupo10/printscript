package printscript.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import printscript.ast.Expression;
import printscript.ast.Identifier;
import printscript.ast.IfStatement;
import printscript.ast.Statement;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

public final class IfStatementParser implements StatementParser {
    private final Supplier<List<StatementParser>> statementParsers;

    public IfStatementParser(Supplier<List<StatementParser>> statementParsers) {
        this.statementParsers = statementParsers;
    }

    @Override
    public boolean canParse(TokenStream tokens) {
        return tokens.peek().type() == TokenType.IF;
    }

    @Override
    public Statement parse(TokenStream tokens, ExpressionParser expressionParser) {
        Token ifToken = tokens.consume();
        tokens.expect(TokenType.LEFT_PAREN);
        Token conditionToken = tokens.expect(TokenType.IDENTIFIER);
        Expression condition = new Identifier(conditionToken.value(), conditionToken.span());
        tokens.expect(TokenType.RIGHT_PAREN);

        tokens.expect(TokenType.LEFT_BRACE);
        List<Statement> thenBranch = parseBlockBody(tokens, expressionParser);
        Token lastToken = tokens.expect(TokenType.RIGHT_BRACE);

        Optional<List<Statement>> elseBranch = Optional.empty();
        if (tokens.peek().type() == TokenType.ELSE) {
            tokens.consume();
            tokens.expect(TokenType.LEFT_BRACE);
            List<Statement> elseStatements = parseBlockBody(tokens, expressionParser);
            lastToken = tokens.expect(TokenType.RIGHT_BRACE);
            elseBranch = Optional.of(elseStatements);
        }

        Span span = Span.merge(ifToken.span(), lastToken.span());
        return new IfStatement(condition, thenBranch, elseBranch, span);
    }

    private List<Statement> parseBlockBody(TokenStream tokens, ExpressionParser expressionParser) {
        List<Statement> statements = new ArrayList<>();
        while (tokens.peek().type() != TokenType.RIGHT_BRACE) {
            statements.add(
                    StatementDispatcher.dispatch(tokens, statementParsers.get(), expressionParser));
        }
        return statements;
    }
}
