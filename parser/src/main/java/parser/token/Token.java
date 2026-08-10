package parser.token;

public interface Token {
    TokenType getType();
    String getValue();
    Position getStart();
    Position getEnd();
}