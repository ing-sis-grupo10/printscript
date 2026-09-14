package printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class PrintScriptFormatterTest {

    private String format(String source, FormattingRules rules) {
        var formatter = new PrintScriptFormatter(rules);
        var writer = new StringWriter();
        formatter.format(new StringReader(source), writer);
        return writer.toString();
    }

    @Test
    void formatsWithAllSpacingRulesOn() {
        String source = "let     a:number=13*4\n;\nlet   b :string  =   \"hi\" ;";
        var rules = new FormattingRules(true, true, true, true, 1, true, 2);

        String result = format(source, rules);

        assertEquals("let a : number = 13 * 4;\nlet b : string = \"hi\";", result);
    }

    @Test
    void formatsWithAllSpacingRulesOff() {
        String source = "let   a : number = 2 + 2 ;";
        var rules = new FormattingRules(false, false, false, false, 1, true, 2);

        String result = format(source, rules);

        assertEquals("let a:number=2 + 2;", result);
    }

    @Test
    void spaceBeforeColonOnlyAppliesBeforeNotAfter() {
        String source = "let a:number=5;";
        var rules = new FormattingRules(true, false, false, false, 1, true, 2);

        String result = format(source, rules);

        assertEquals("let a :number=5;", result);
    }

    @Test
    void spaceAfterAssignOnlyAppliesAfterNotBefore() {
        String source = "let a:number=5;";
        var rules = new FormattingRules(false, false, false, true, 1, true, 2);

        String result = format(source, rules);

        assertEquals("let a:number= 5;", result);
    }

    @Test
    void printlnWithZeroBlankLinesBefore() {
        String source = "let a: string;\nprintln(a);";
        var rules = new FormattingRules(true, true, true, true, 0, true, 2);

        String result = format(source, rules);

        assertEquals("let a : string;\nprintln(a);", result);
    }

    @Test
    void printlnWithTwoBlankLinesBefore() {
        String source = "let a: string;\nprintln(a);";
        var rules = new FormattingRules(true, true, true, true, 2, true, 2);

        String result = format(source, rules);

        assertEquals("let a : string;\n\n\nprintln(a);", result);
    }

    @Test
    void parenthesesNeverHaveSpaceAroundThem() {
        String source = "println(\"hola\");";
        var rules = FormattingRules.defaults();

        String result = format(source, rules);

        assertEquals("println(\"hola\");", result);
    }

    @Test
    void ifBraceGoesOnNextLineWhenConfigured() {
        String source =
                "let something: boolean = true;\nif (something) {\nprintln(\"Entered if\");\n}";
        var rules = new FormattingRules(true, true, true, true, 1, false, 2);

        String result = format(source, rules);

        assertEquals(
                "let something : boolean = true;\nif (something)\n{\n  println(\"Entered if\");\n}",
                result);
    }

    @Test
    void ifBraceStaysOnSameLineByDefault() {
        String source =
                "let something: boolean = true;\nif (something)\n{\nprintln(\"Entered if\");\n}";
        var rules = FormattingRules.defaults();

        String result = format(source, rules);

        assertEquals(
                "let something : boolean = true;\nif (something) {\n  println(\"Entered if\");\n}",
                result);
    }

    @Test
    void indentsNestedIfBlocksAccordingToConfiguredSize() {
        String source =
                "let something: boolean = true;\nif (something) {\nif (something) {\nprintln(\"Entered two ifs\");\n}\n}";
        var rules = new FormattingRules(true, true, true, true, 1, true, 4);

        String result = format(source, rules);

        assertEquals(
                "let something : boolean = true;\nif (something) {\n    if (something) {\n        println(\"Entered two ifs\");\n    }\n}",
                result);
    }
}
