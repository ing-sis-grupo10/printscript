package printscript.ast;

import printscript.common.token.Span;

public record StringLiteral(String value, Span span) implements Expression {}