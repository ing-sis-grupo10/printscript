package edu.austral.printscript.common.token;

public record Token(TokenType type, String value, Position position) {
}