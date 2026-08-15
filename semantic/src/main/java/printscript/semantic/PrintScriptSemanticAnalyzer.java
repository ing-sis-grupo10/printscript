package printscript.semantic;

import printscript.ast.Assignment;
import printscript.ast.BinaryExpression;
import printscript.ast.DeclaredType;
import printscript.ast.Expression;
import printscript.ast.Identifier;
import printscript.ast.NumberLiteral;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.ast.StringLiteral;
import printscript.ast.VariableDeclaration;
import printscript.diagnostics.Diagnostic;
import printscript.diagnostics.DiagnosticReporter;

import java.util.Iterator;
import java.util.Optional;

public final class PrintScriptSemanticAnalyzer implements SemanticAnalyzer {
    private final Iterator<Statement> statements;
    private final SymbolTable symbols;
    private final DiagnosticReporter reporter;

    public PrintScriptSemanticAnalyzer(Iterator<Statement> statements, SymbolTable symbols, DiagnosticReporter reporter) {
        this.statements = statements;
        this.symbols = symbols;
        this.reporter = reporter;
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Statement next() {
        Statement statement = statements.next();
        check(statement);
        return statement;
    }

    private void check(Statement statement) {
        switch (statement) {
            case VariableDeclaration declaration -> checkVariableDeclaration(declaration);
            case Assignment assignment -> checkAssignment(assignment);
            case PrintlnStatement println -> typeOf(println.argument());
        }
    }

    private void checkVariableDeclaration(VariableDeclaration declaration) {
        Type declaredType = toSemanticType(declaration.declaredType());

        declaration.initializer().ifPresent(initializer -> {
            Type initializerType = typeOf(initializer);
            if (initializerType != Type.UNKNOWN && initializerType != declaredType) {
                reporter.report(Diagnostic.error(
                        "No se puede asignar " + initializerType + " a una variable de tipo " + declaredType,
                        initializer.span()));
            }
        });

        symbols.declare(declaration.name(), declaredType, declaration.span(), reporter);
    }

    private void checkAssignment(Assignment assignment) {
        Type valueType = typeOf(assignment.value());
        Optional<Type> declaredType = symbols.lookup(assignment.name());

        if (declaredType.isEmpty()) {
            reporter.report(Diagnostic.error("Variable no declarada: " + assignment.name(), assignment.span()));
            return;
        }

        if (valueType != Type.UNKNOWN && valueType != declaredType.get()) {
            reporter.report(Diagnostic.error(
                    "No se puede asignar " + valueType + " a " + assignment.name()
                            + " (declarada como " + declaredType.get() + ")",
                    assignment.value().span()));
        }
    }

    private Type typeOf(Expression expression) {
        return switch (expression) {
            case NumberLiteral n -> Type.NUMBER;
            case StringLiteral s -> Type.STRING;
            case Identifier id -> symbols.lookup(id.name())
                    .orElseGet(() -> {
                        reporter.report(Diagnostic.error("Variable no declarada: " + id.name(), id.span()));
                        return Type.UNKNOWN;
                    });
            case BinaryExpression b -> typeOfBinary(b);
        };
    }

    private Type typeOfBinary(BinaryExpression expression) {
        Type leftType = typeOf(expression.left());
        Type rightType = typeOf(expression.right());

        if (leftType == Type.UNKNOWN || rightType == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }

        return switch (expression.operator()) {
            case PLUS -> typeOfPlus(leftType, rightType);
            case MINUS, TIMES, DIVIDE -> typeOfArithmetic(leftType, rightType, expression);
        };
    }

    private Type typeOfPlus(Type left, Type right) {
        if (left == Type.STRING || right == Type.STRING) {
            return Type.STRING;
        }
        return Type.NUMBER;
    }

    private Type typeOfArithmetic(Type left, Type right, BinaryExpression expression) {
        if (left != Type.NUMBER || right != Type.NUMBER) {
            reporter.report(Diagnostic.error(
                    "Los operandos de " + expression.operator() + " deben ser number", expression.span()));
            return Type.UNKNOWN;
        }
        return Type.NUMBER;
    }

    private Type toSemanticType(DeclaredType declaredType) {
        return switch (declaredType) {
            case NUMBER -> Type.NUMBER;
            case STRING -> Type.STRING;
        };
    }
}