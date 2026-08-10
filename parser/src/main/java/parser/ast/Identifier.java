package parser.ast;

import parser.token.Position;

public record Identifier(String name, Position start, Position end) implements Expression {

    @Override
    public Position getStart() {
        return start;
    }

    @Override
    public Position getEnd() {
        return end;
    }
}