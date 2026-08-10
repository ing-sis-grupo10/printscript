package parser.ast;

import parser.token.Position;

public record VariableAssignment(String name, Expression value, Position start, Position end) implements ASTNode {

    @Override
    public Position getStart() {
        return start;
    }

    @Override
    public Position getEnd() {
        return end;
    }
}