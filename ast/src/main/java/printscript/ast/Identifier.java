package printscript.ast;

import printscript.diagnostics.Span;

public record Identifier(String name, Span span) implements Expression {}