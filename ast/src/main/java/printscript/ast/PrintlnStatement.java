package printscript.ast;

import edu.austral.printscript.common.token.Span;

public record PrintlnStatement(Expression argument, Span span) implements Statement {}