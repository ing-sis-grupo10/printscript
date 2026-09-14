package printscript.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

class AnalyzerRulesLoaderTest {

    private final AnalyzerRulesLoader loader = new AnalyzerRulesLoader();

    @Test
    void loadsRulesFromJson() {
        String json =
                """
            {
              "identifier_format": "snake_case",
              "println_identifier_or_literal_only": false,
              "identifier_case_enabled": false
            }
            """;

        AnalyzerRules rules = loader.load(new StringReader(json));

        assertEquals(AnalyzerRules.IdentifierCase.SNAKE_CASE, rules.identifierCase());
        assertFalse(rules.printlnOnlyIdentifierOrLiteral());
        assertFalse(rules.identifierCaseEnabled());
    }

    @Test
    void loaderUsesDefaultsForMissingKeys() {
        AnalyzerRules rules = loader.load(new StringReader("{}"));

        assertEquals(AnalyzerRules.defaults(), rules);
    }
}
