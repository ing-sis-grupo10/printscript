package printscript.parser;

import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

import java.util.Iterator;

class TokenStream {

    private final Iterator<Token> source;
    private Token current;

    TokenStream(Iterator<Token> source) {
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
            throw new ParseError("Expected " + type + " but found " + current.type(), current.span());
        }
        return consume();
    }

    private Token advance() {
        return source.hasNext() ? source.next() : eof();
    }

    private Token eof() {
        Span eofSpan = current == null
                ? Span.of(new Position(0, 0), new Position(0, 0))
                : current.span();
        return new Token(TokenType.EOF, "", eofSpan);
    }
}
