package printscript.ast;

import printscript.diagnostics.Span;

public record StringLiteral(String value, Span span) implements Expression {}