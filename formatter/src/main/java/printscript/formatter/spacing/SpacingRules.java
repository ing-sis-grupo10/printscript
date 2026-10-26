package printscript.formatter.spacing;

import java.util.List;
import printscript.common.token.TokenType;
import printscript.formatter.FormattingRules;

// Arma la cadena de reglas a partir de la config. El orden importa: gana la primera que responde.
public final class SpacingRules {

    private SpacingRules() {}

    public static List<SpacingRule> from(FormattingRules config) {
        return List.of(
                new IfBracePlacementRule(config.ifBraceSameLine()),
                new BlockLineBreakRule(),
                new LineBreakAfterSemicolonRule(config.blankLinesAfterPrintln()),
                new NoSpaceBeforeSemicolonRule(),
                new IfKeywordSpacingRule(),
                new OperatorSpacingRule(),
                new AroundTokenSpacingRule(
                        TokenType.COLON,
                        config.spaceBeforeColon(),
                        config.spaceAfterColon(),
                        config.singleSpaceSeparation()),
                new AroundTokenSpacingRule(
                        TokenType.ASSIGN,
                        config.spaceBeforeAssign(),
                        config.spaceAfterAssign(),
                        config.singleSpaceSeparation()),
                new ParenthesisSpacingRule(config.singleSpaceSeparation()),
                new SingleSpaceFallbackRule());
    }
}
