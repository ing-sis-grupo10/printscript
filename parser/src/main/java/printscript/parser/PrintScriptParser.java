package printscript.parser;

import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.ast.Statement;
import printscript.diagnostics.Diagnostic;
import printscript.diagnostics.DiagnosticReporter;

import java.util.Iterator;
import java.util.List;

public final class PrintScriptParser implements Iterator<Statement> {
    private final TokenStream tokens;
    private final List<StatementParser> statementParsers;
    private final ExpressionParser expressionParser;
    private final DiagnosticReporter reporter;

    public PrintScriptParser(Iterator<Token> tokenSource, List<StatementParser> statementParsers,
                             ExpressionParser expressionParser, DiagnosticReporter reporter) {
        this.tokens = new TokenStream(tokenSource);
        this.statementParsers = statementParsers;
        this.expressionParser = expressionParser;
        this.reporter = reporter;
    }

    @Override
    public boolean hasNext() {
        return tokens.peek().type() != TokenType.EOF;
    }

    @Override
    public Statement next() {
        for (StatementParser statementParser : statementParsers) {
            if (statementParser.canParse(tokens)) {
                try {
                    return statementParser.parse(tokens, expressionParser, reporter);
                } catch (ParseError e) {
                    reporter.report(Diagnostic.error(e.getMessage(), e.span()));
                    recoverToNextStatement();
                    return next();
                }
            }
        }

        Token unexpected = tokens.consume();
        reporter.report(Diagnostic.error("No se esperaba: " + unexpected.value(), unexpected.span()));
        return next();
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
