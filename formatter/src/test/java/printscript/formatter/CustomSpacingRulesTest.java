package printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import printscript.common.Version;
import printscript.formatter.spacing.SingleSpaceFallbackRule;
import printscript.formatter.spacing.SpacingRule;

class CustomSpacingRulesTest {

    private String format(String source, List<SpacingRule> rules) {
        var writer = new StringWriter();
        new PrintScriptFormatter(rules, 2).format(new StringReader(source), writer, Version.V1_1);
        return writer.toString();
    }

    @Test
    void usesOnlyTheInjectedRules() {
        String result = format("let x:number=5;", List.of(new SingleSpaceFallbackRule()));

        assertEquals("let x : number = 5 ;", result);
    }

    @Test
    void firstRuleThatAnswersWins() {
        SpacingRule noSpaces = (previous, current, context) -> Optional.of("");

        String result =
                format("let x: number = 5;", List.of(noSpaces, new SingleSpaceFallbackRule()));

        assertEquals("letx:number=5;", result);
    }

    @Test
    void failsWhenNoRuleDecides() {
        assertThrows(IllegalStateException.class, () -> format("let x: number;", List.of()));
    }
}
