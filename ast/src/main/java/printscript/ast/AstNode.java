package printscript.ast;

import printscript.common.token.Span;

public sealed interface AstNode permits Statement, Expression {
    Span span();
}
