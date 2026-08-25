package printscript.formatter;

public record FormattingRules(
        boolean spaceBeforeColon,
        boolean spaceAfterColon,
        boolean spaceBeforeAssign,
        boolean spaceAfterAssign,
        int blankLinesBeforePrintln) {
    public static FormattingRules defaults() {
        return new FormattingRules(true, true, true, true, 1);
    }
}
