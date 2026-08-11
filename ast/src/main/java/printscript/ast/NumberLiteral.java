package printscript.ast;

import printscript.diagnostics.Span;

import java.math.BigDecimal;

public record NumberLiteral(BigDecimal value, Span span) implements Expression {}