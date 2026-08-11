package printscript.ast;

import printscript.diagnostics.Span;

public record PrintlnStatement(Expression argument, Span span) implements Statement {}