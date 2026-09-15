package printscript.ast;

import printscript.common.token.Span;

public record ReadEnvExpression(Expression name, Span span) implements Expression {}
