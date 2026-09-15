package printscript.formatter;

import java.util.Optional;

public record FormattingRules(
        Optional<Boolean> spaceBeforeColon,
        Optional<Boolean> spaceAfterColon,
        Optional<Boolean> spaceBeforeAssign,
        Optional<Boolean> spaceAfterAssign,
        boolean singleSpaceSeparation,
        int blankLinesBeforePrintln,
        boolean ifBraceSameLine,
        int indentSizeInsideIf) {
    public static FormattingRules defaults() {
        return new FormattingRules(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                1,
                true,
                2);
    }
}
