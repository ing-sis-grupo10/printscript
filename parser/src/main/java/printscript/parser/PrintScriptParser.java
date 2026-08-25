package printscript.parser;

import printscript.ast.Statement;
import printscript.common.result.Diagnostic;
import printscript.common.result.Result;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

import java.util.Iterator;
import java.util.List;

public final class PrintScriptParser implements Iterator<Result<Statement>> {
    private final TokenStream tokens;
    private final List<StatementParser> statementParsers;
    private final ExpressionParser expressionParser;

    public PrintScriptParser(Iterator<Result<Token>> tokenSource, List<StatementParser> statementParsers,
                             ExpressionParser expressionParser) {
        this.tokens = new TokenStream(tokenSource);
        this.statementParsers = statementParsers;
        this.expressionParser = expressionParser;
    }

    @Override
    public boolean hasNext() {
        return tokens.peek().type() != TokenType.EOF;
    }

    @Override
    public Result<Statement> next() {
        for (StatementParser statementParser : statementParsers) {
            if (statementParser.canParse(tokens)) {
                try {
                    return Result.success(statementParser.parse(tokens, expressionParser));
                } catch (ParseError e) {
                    recoverToNextStatement();
                    return Result.failure(Diagnostic.error(e.getMessage(), e.span()));
                }
            }
        }

        Token unexpected = tokens.consume();
        recoverToNextStatement();
        return Result.failure(Diagnostic.error("No se esperaba: " + unexpected.value(), unexpected.span()));
    }

    private void recoverToNextStatement() {
        while (hasNext() && tokens.peek().type() != TokenType.SEMICOLON) {
            tokens.consume();
        }
        if (hasNext()) {
            tokens.consume();
        }
    }
}