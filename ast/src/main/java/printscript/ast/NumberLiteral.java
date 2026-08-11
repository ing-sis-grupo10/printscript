package printscript.ast;

import edu.austral.printscript.common.token.Span;

import java.math.BigDecimal;

public record NumberLiteral(BigDecimal value, Span span) implements Expression {}