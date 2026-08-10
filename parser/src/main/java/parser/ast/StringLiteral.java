package parser.ast;

import parser.token.Position;

public record StringLiteral(String value, Position start, Position end) implements Expression {

    @Override
    public Position getStart() {
        return start;
    }

    @Override
    public Position getEnd() {
        return end;
    }
}