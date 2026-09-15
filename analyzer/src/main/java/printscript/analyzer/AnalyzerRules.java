package printscript.analyzer;

public record AnalyzerRules(
        IdentifierCase identifierCase,
        boolean identifierCaseEnabled,
        boolean printlnOnlyIdentifierOrLiteral,
        boolean readInputOnlyIdentifierOrLiteral) {
    public enum IdentifierCase {
        CAMEL_CASE,
        SNAKE_CASE
    }

    public static AnalyzerRules defaults() {
        return new AnalyzerRules(IdentifierCase.CAMEL_CASE, true, true, true);
    }
}
