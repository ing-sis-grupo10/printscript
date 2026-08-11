package printscript.ast;

import edu.austral.printscript.common.token.Span;

public record Identifier(String name, Span span) implements Expression {}