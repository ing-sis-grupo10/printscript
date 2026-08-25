package printscript.ast;

import printscript.common.token.Span;

public record PrintlnStatement(Expression argument, Span span) implements Statement {}
