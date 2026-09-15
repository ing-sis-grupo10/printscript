package printscript.formatter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.Optional;

public final class FormattingRulesLoader {

    public FormattingRules load(Reader configReader) {
        JsonObject json = JsonParser.parseReader(configReader).getAsJsonObject();

        Optional<Boolean> spaceBeforeColon =
                getOptionalBoolean(json, "declaration_space_before_colon");
        Optional<Boolean> spaceAfterColon =
                getOptionalBoolean(json, "declaration_space_after_colon");
        Optional<Boolean> spaceBeforeAssign =
                getOptionalBoolean(json, "assignment_space_before_equals");
        Optional<Boolean> spaceAfterAssign =
                getOptionalBoolean(json, "assignment_space_after_equals");
        boolean singleSpaceSeparation = getBoolean(json, "single_space_separation", false);
        int blankLinesAfterPrintln = getInt(json, "println_new_lines_after_call", 1);
        boolean ifBraceSameLine = getBoolean(json, "if_brace_same_line", true);
        int indentSizeInsideIf = getInt(json, "if_indent_size", 2);

        return new FormattingRules(
                spaceBeforeColon,
                spaceAfterColon,
                spaceBeforeAssign,
                spaceAfterAssign,
                singleSpaceSeparation,
                Math.min(blankLinesAfterPrintln, 2),
                ifBraceSameLine,
                indentSizeInsideIf);
    }

    private Optional<Boolean> getOptionalBoolean(JsonObject json, String key) {
        return json.has(key) ? Optional.of(json.get(key).getAsBoolean()) : Optional.empty();
    }

    private boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        return json.has(key) ? json.get(key).getAsBoolean() : defaultValue;
    }

    private int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) ? json.get(key).getAsInt() : defaultValue;
    }
}
