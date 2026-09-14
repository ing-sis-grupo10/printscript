package printscript.formatter;

public record FormattingRules(
        boolean spaceBeforeColon,
        boolean spaceAfterColon,
        boolean spaceBeforeAssign,
        boolean spaceAfterAssign,
        int blankLinesBeforePrintln,
        boolean ifBraceSameLine,
        int indentSizeInsideIf) {
    public static FormattingRules defaults() {
        return new FormattingRules(true, true, true, true, 1, true, 2);
    }
}
