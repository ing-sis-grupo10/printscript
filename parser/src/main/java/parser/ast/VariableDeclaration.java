package parser.ast;

import parser.token.Position;

import java.util.Optional;

public record VariableDeclaration(String name, String varType, Optional<Expression> initializer, Position start, Position end) implements ASTNode {

    @Override
    public Position getStart() {
        return start;
    }

    @Override
    public Position getEnd() {
        return end;
    }
}