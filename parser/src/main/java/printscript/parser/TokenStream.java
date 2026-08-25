package printscript.parser;

import java.util.Iterator;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class TokenStream {

    private final Iterator<Result<Token>> source;
    private Token current;

    TokenStream(Iterator<Result<Token>> source) {
        this.source = source;
        this.current = advance();
    }

    Token peek() {
        return current;
    }

    Token consume() {
        Token token = current;
        current = advance();
        return token;
    }

    Token expect(TokenType type) {
        if (current.type() != type) {
            throw new ParseError(
                    "Expected " + type + " but found " + current.type(), current.span());
        }
        return consume();
    }

    private Token advance() {
        if (!source.hasNext()) {
            return eof();
        }
        Result<Token> result = source.next();
        return switch (result) {
            case Success<Token> s -> s.value();
            case Failure<Token> f -> {
                Diagnostic diagnostic = f.diagnostics().get(0);
                throw new ParseError(diagnostic.message(), diagnostic.span());
            }
        };
    }

    private Token eof() {
        Span eofSpan =
                current == null ? Span.of(new Position(0, 0), new Position(0, 0)) : current.span();
        return new Token(TokenType.EOF, "", eofSpan);
    }
}
