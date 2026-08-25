package printscript.common.result;

public record Success<T>(T value) implements Result<T> {}
