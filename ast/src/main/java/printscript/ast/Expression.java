package printscript.ast;

public sealed interface Expression extends AstNode
        permits NumberLiteral, StringLiteral, Identifier, BinaryExpression {}