package printscript.ast;

import java.math.BigDecimal;
import printscript.common.token.Span;

public record NumberLiteral(BigDecimal value, Span span) implements Expression {}
