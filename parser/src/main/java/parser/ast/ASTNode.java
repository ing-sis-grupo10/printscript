package parser.ast;

import parser.token.Position;

public interface ASTNode {
    Position getStart();
    Position getEnd();
}