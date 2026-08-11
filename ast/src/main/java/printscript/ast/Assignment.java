package printscript.ast;

import edu.austral.printscript.common.token.Span;

public record Assignment(String name, Expression value, Span span) implements Statement {}