package printscript.ast;

import printscript.common.token.Span;

import java.math.BigDecimal;

public record NumberLiteral(BigDecimal value, Span span) implements Expression {}