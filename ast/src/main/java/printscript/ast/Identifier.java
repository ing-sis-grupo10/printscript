package printscript.ast;

import printscript.common.token.Span;

public record Identifier(String name, Span span) implements Expression {}