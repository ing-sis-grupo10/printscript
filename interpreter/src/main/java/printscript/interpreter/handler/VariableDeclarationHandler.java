package printscript.interpreter.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import printscript.ast.DeclaredType;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.runtime.Environment;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.RuntimeValue;

public final class VariableDeclarationHandler implements StatementHandler {
    private final ExpressionEvaluator evaluator;

    public VariableDeclarationHandler(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public boolean canHandle(Statement statement) {
        return statement instanceof VariableDeclaration;
    }

    @Override
    public Result<Statement> handle(Statement statement, Environment environment) {
        VariableDeclaration declaration = (VariableDeclaration) statement;
        DeclaredType declaredType = declaration.declaredType();
        List<Diagnostic> diagnostics = new ArrayList<>();
        Optional<RuntimeValue> initializerValue = Optional.empty();

        if (declaration.initializer().isPresent()) {
            var initializer = declaration.initializer().get();
            Result<RuntimeValue> value = evaluator.evaluate(initializer, environment);
            switch (value) {
                case Failure<RuntimeValue> f -> diagnostics.addAll(f.diagnostics());
                case Success<RuntimeValue> s -> {
                    if (evaluator.typeOf(s.value()) == declaredType) {
                        initializerValue = Optional.of(s.value());
                    } else {
                        diagnostics.add(
                                Diagnostic.error(
                                        "No se puede asignar "
                                                + evaluator.typeOf(s.value())
                                                + " a una variable de tipo "
                                                + declaredType,
                                        initializer.span()));
                    }
                }
            }
        }

        environment
                .declare(declaration.name(), declaredType, declaration.span())
                .ifPresent(diagnostics::add);

        if (!diagnostics.isEmpty()) {
            return Result.failure(diagnostics);
        }

        initializerValue.ifPresent(value -> environment.assign(declaration.name(), value));
        return Result.success(statement);
    }
}
