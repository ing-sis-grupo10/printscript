package printscript.formatter;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormattingRulesLoaderTest {

    private final FormattingRulesLoader loader = new FormattingRulesLoader();

    @Test
    void loadsRulesFromJson() {
        String json = """
                {
                  "declaration_space_before_colon": false,
                  "declaration_space_after_colon": true,
                  "assignment_space_before_equals": true,
                  "assignment_space_after_equals": false,
                  "println_new_lines_before_call": 2
                }
                """;

        FormattingRules rules = loader.load(new StringReader(json));

        assertFalse(rules.spaceBeforeColon());
        assertTrue(rules.spaceAfterColon());
        assertTrue(rules.spaceBeforeAssign());
        assertFalse(rules.spaceAfterAssign());
        assertEquals(2, rules.blankLinesBeforePrintln());
    }

    @Test
    void loaderUsesDefaultsForMissingKeys() {
        FormattingRules rules = loader.load(new StringReader("{}"));

        assertEquals(FormattingRules.defaults(), rules);
    }

    @Test
    void loaderClampsBlankLinesToMaxTwo() {
        FormattingRules rules = loader.load(new StringReader("{\"println_new_lines_before_call\": 5}"));

        assertEquals(2, rules.blankLinesBeforePrintln());
    }
}