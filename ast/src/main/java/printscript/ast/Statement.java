package printscript.ast;

public sealed interface Statement extends AstNode
        permits VariableDeclaration, Assignment, PrintlnStatement {}
