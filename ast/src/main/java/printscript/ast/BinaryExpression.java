package printscript.ast;

import printscript.common.token.Span;

public record BinaryExpression(
        Expression left,
        BinaryOperator operator,
        Expression right,
        Span span
) implements Expression {}