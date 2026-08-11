package printscript.ast;

import printscript.diagnostics.Span;

public sealed interface AstNode permits Statement, Expression {
    Span span();
}