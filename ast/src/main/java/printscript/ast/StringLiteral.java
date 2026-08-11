package printscript.ast;

import edu.austral.printscript.common.token.Span;

public record StringLiteral(String value, Span span) implements Expression {}