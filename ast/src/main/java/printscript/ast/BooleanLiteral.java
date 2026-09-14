package printscript.ast;

import printscript.common.token.Span;

public record BooleanLiteral(boolean value, Span span) implements Expression {}
