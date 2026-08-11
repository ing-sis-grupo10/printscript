package printscript.ast;

import printscript.diagnostics.Span;

public record Assignment(String name, Expression value, Span span) implements Statement {}