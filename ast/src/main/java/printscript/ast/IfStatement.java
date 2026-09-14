package printscript.ast;

import java.util.List;
import java.util.Optional;
import printscript.common.token.Span;

public record IfStatement(
        Expression condition,
        List<Statement> thenBranch,
        Optional<List<Statement>> elseBranch,
        Span span)
        implements Statement {

    public IfStatement {
        thenBranch = List.copyOf(thenBranch);
        elseBranch = elseBranch.map(List::copyOf);
    }
}
