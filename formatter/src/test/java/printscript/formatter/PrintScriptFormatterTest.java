package printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PrintScriptFormatterTest {

    private String format(String source, FormattingRules rules) {
        var formatter = new PrintScriptFormatter(rules);
        var writer = new StringWriter();
        formatter.format(new StringReader(source), writer, "1.1");
        return writer.toString();
    }

    @Test
    void formatsWithAllSpacingRulesOn() {
        String source = "let     a:number=13*4\n;\nlet   b :string  =   \"hi\" ;";
        var rules =
                new FormattingRules(
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        false,
                        1,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals("let a : number = 13 * 4;\nlet b : string = \"hi\";", result);
    }

    @Test
    void formatsWithAllSpacingRulesOff() {
        String source = "let   a : number = 2 + 2 ;";
        var rules =
                new FormattingRules(
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        false,
                        1,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals("let a:number=2 + 2;", result);
    }

    @Test
    void spaceBeforeColonOnlyAppliesBeforeNotAfter() {
        String source = "let a:number=5;";
        var rules =
                new FormattingRules(
                        Optional.of(true),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        false,
                        1,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals("let a :number=5;", result);
    }

    @Test
    void spaceAfterAssignOnlyAppliesAfterNotBefore() {
        String source = "let a:number=5;";
        var rules =
                new FormattingRules(
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(false),
                        Optional.of(true),
                        false,
                        1,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals("let a:number= 5;", result);
    }

    @Test
    void noBlankLinesAfterPrintlnWhenConfiguredToZero() {
        String source = "println(a);\nprintln(b);";
        var rules =
                new FormattingRules(
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        false,
                        0,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals("println(a);\nprintln(b);", result);
    }

    @Test
    void twoBlankLinesAfterPrintlnWhenConfiguredToTwo() {
        String source = "println(a);\nprintln(b);";
        var rules =
                new FormattingRules(
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        false,
                        2,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals("println(a);\n\n\nprintln(b);", result);
    }

    @Test
    void blankLinesAfterPrintlnDoNotApplyWhenPrecedingStatementIsNotPrintln() {
        String source = "let a: string = \"hi\";\nprintln(a);";
        var rules =
                new FormattingRules(
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        false,
                        2,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals("let a : string = \"hi\";\nprintln(a);", result);
    }

    @Test
    void parenthesesNeverHaveSpaceAroundThemWhenNotConfigured() {
        String source = "println(\"hola\");";
        var rules = FormattingRules.defaults();

        String result = format(source, rules);

        assertEquals("println(\"hola\");", result);
    }

    @Test
    void ifBraceGoesOnNextLineWhenConfigured() {
        String source =
                "let something: boolean = true;\nif (something) {\nprintln(\"Entered if\");\n}";
        var rules =
                new FormattingRules(
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        false,
                        1,
                        false,
                        2);

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
                "let something: boolean = true;\nif (something) {\n  println(\"Entered if\");\n}",
                result);
    }

    @Test
    void indentsNestedIfBlocksAccordingToConfiguredSize() {
        String source =
                "let something: boolean = true;\nif (something) {\nif (something) {\nprintln(\"Entered two ifs\");\n}\n}";
        var rules =
                new FormattingRules(
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        Optional.of(true),
                        false,
                        1,
                        true,
                        4);

        String result = format(source, rules);

        assertEquals(
                "let something : boolean = true;\nif (something) {\n    if (something) {\n        println(\"Entered two ifs\");\n    }\n}",
                result);
    }

    @Test
    void unconfiguredColonSpacingPreservesOriginalPerOccurrence() {
        String source = "let a:number = 1;\nlet b: number = 2;\nlet c : number = 3;";
        var rules = FormattingRules.defaults();

        String result = format(source, rules);

        assertEquals("let a:number = 1;\nlet b: number = 2;\nlet c : number = 3;", result);
    }

    @Test
    void singleSpaceSeparationForcesExactlyOneSpaceEverywhereIncludingParens() {
        String source = "let something:      string=\"a really cool thing\";\nprintln(something);";
        var rules =
                new FormattingRules(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        true,
                        0,
                        true,
                        2);

        String result = format(source, rules);

        assertEquals(
                "let something : string = \"a really cool thing\";\nprintln ( something );",
                result);
    }
}
