package printscript.ast;

import printscript.common.token.Span;

public record ReadInputExpression(Expression message, Span span) implements Expression {}
