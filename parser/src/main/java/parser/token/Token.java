package parser.token;
import printscript.diagnostics.Span;

public interface Token {
    TokenType getType();
    String getValue();
    Span getSpan();
}