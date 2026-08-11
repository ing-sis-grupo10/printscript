package printscript.ast;

import edu.austral.printscript.common.token.Span;

public sealed interface AstNode permits Statement, Expression {
    Span span();
}