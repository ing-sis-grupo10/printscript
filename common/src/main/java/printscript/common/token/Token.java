package printscript.common.token;

public record Token(TokenType type, String value, Span span) {}
