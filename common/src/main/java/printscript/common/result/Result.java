package printscript.common.result;

import java.util.List;

public sealed interface Result<T> permits Success, Failure {
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(Diagnostic diagnostic) {
        return new Failure<>(List.of(diagnostic));
    }

    static <T> Result<T> failure(List<Diagnostic> diagnostics) {
        return new Failure<>(diagnostics);
    }
}