package printscript.ast;

import printscript.diagnostics.Span;

public record BinaryExpression(
        Expression left,
        BinaryOperator operator,
        Expression right,
        Span span
) implements Expression {}