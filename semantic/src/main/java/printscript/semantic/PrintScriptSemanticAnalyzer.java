package printscript.semantic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;

public final class PrintScriptSemanticAnalyzer implements Iterator<Result<Statement>> {
    private final Iterator<Result<Statement>> statements;
    private final SymbolTable symbols;

    public PrintScriptSemanticAnalyzer(
            Iterator<Result<Statement>> statements, SymbolTable symbols) {
        this.statements = statements;
        this.symbols = symbols;
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Result<Statement> next() {
        Result<Statement> upstream = statements.next();
        return switch (upstream) {
            case Failure<Statement> f -> upstream;
            case Success<Statement> s -> analyze(s.value());
        };
    }

    private Result<Statement> analyze(Statement statement) {
        List<Diagnostic> diagnostics = check(statement);
        return diagnostics.isEmpty() ? Result.success(statement) : Result.failure(diagnostics);
    }

    private List<Diagnostic> check(Statement statement) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        switch (statement) {
            case VariableDeclaration declaration ->
                    checkVariableDeclaration(declaration, diagnostics);
            case Assignment assignment -> checkAssignment(assignment, diagnostics);
            case PrintlnStatement println -> typeOf(println.argument(), diagnostics);
        }
        return diagnostics;
    }

    private void checkVariableDeclaration(
            VariableDeclaration declaration, List<Diagnostic> diagnostics) {
        Type declaredType = toSemanticType(declaration.declaredType());

        declaration
                .initializer()
                .ifPresent(
                        initializer -> {
                            Type initializerType = typeOf(initializer, diagnostics);
                            if (initializerType != Type.UNKNOWN
                                    && initializerType != declaredType) {
                                diagnostics.add(
                                        Diagnostic.error(
                                                "No se puede asignar "
                                                        + initializerType
                                                        + " a una variable de tipo "
                                                        + declaredType,
                                                initializer.span()));
                            }
                        });

        symbols.declare(declaration.name(), declaredType, declaration.span())
                .ifPresent(diagnostics::add);
    }

    private void checkAssignment(Assignment assignment, List<Diagnostic> diagnostics) {
        Type valueType = typeOf(assignment.value(), diagnostics);
        Optional<Type> declaredType = symbols.lookup(assignment.name());

        if (declaredType.isEmpty()) {
            diagnostics.add(
                    Diagnostic.error(
                            "Variable no declarada: " + assignment.name(), assignment.span()));
            return;
        }

        if (valueType != Type.UNKNOWN && valueType != declaredType.get()) {
            diagnostics.add(
                    Diagnostic.error(
                            "No se puede asignar "
                                    + valueType
                                    + " a "
                                    + assignment.name()
                                    + " (declarada como "
                                    + declaredType.get()
                                    + ")",
                            assignment.value().span()));
        }
    }

    private Type typeOf(Expression expression, List<Diagnostic> diagnostics) {
        return switch (expression) {
            case NumberLiteral n -> Type.NUMBER;
            case StringLiteral s -> Type.STRING;
            case Identifier id ->
                    symbols.lookup(id.name())
                            .orElseGet(
                                    () -> {
                                        diagnostics.add(
                                                Diagnostic.error(
                                                        "Variable no declarada: " + id.name(),
                                                        id.span()));
                                        return Type.UNKNOWN;
                                    });
            case BinaryExpression b -> typeOfBinary(b, diagnostics);
        };
    }

    private Type typeOfBinary(BinaryExpression expression, List<Diagnostic> diagnostics) {
        Type leftType = typeOf(expression.left(), diagnostics);
        Type rightType = typeOf(expression.right(), diagnostics);

        if (leftType == Type.UNKNOWN || rightType == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }

        return switch (expression.operator()) {
            case PLUS -> typeOfPlus(leftType, rightType);
            case MINUS, TIMES, DIVIDE ->
                    typeOfArithmetic(leftType, rightType, expression, diagnostics);
        };
    }

    private Type typeOfPlus(Type left, Type right) {
        if (left == Type.STRING || right == Type.STRING) {
            return Type.STRING;
        }
        return Type.NUMBER;
    }

    private Type typeOfArithmetic(
            Type left, Type right, BinaryExpression expression, List<Diagnostic> diagnostics) {
        if (left != Type.NUMBER || right != Type.NUMBER) {
            diagnostics.add(
                    Diagnostic.error(
                            "Los operandos de " + expression.operator() + " deben ser number",
                            expression.span()));
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
