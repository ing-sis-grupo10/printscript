package printscript.formatter;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        var rules = new FormattingRules(true, true, true, true, 1);

        String result = format(source, rules);

        assertEquals("let a : number = 13 * 4;\nlet b : string = \"hi\";", result);
    }

    @Test
    void formatsWithAllSpacingRulesOff() {
        String source = "let   a : number = 2 + 2 ;";
        var rules = new FormattingRules(false, false, false, false, 1);

        String result = format(source, rules);

        assertEquals("let a:number=2 + 2;", result);
    }

    @Test
    void spaceBeforeColonOnlyAppliesBeforeNotAfter() {
        String source = "let a:number=5;";
        var rules = new FormattingRules(true, false, false, false, 1);

        String result = format(source, rules);

        assertEquals("let a :number=5;", result);
    }

    @Test
    void spaceAfterAssignOnlyAppliesAfterNotBefore() {
        String source = "let a:number=5;";
        var rules = new FormattingRules(false, false, false, true, 1);

        String result = format(source, rules);

        assertEquals("let a:number= 5;", result);
    }

    @Test
    void printlnWithZeroBlankLinesBefore() {
        String source = "let a: string;\nprintln(a);";
        var rules = new FormattingRules(true, true, true, true, 0);

        String result = format(source, rules);

        assertEquals("let a : string;\nprintln(a);", result);
    }

    @Test
    void printlnWithTwoBlankLinesBefore() {
        String source = "let a: string;\nprintln(a);";
        var rules = new FormattingRules(true, true, true, true, 2);

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
}