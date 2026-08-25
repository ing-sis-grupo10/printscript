package printscript.formatter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;

public final class FormattingRulesLoader {

    public FormattingRules load(Reader configReader) {
        JsonObject json = JsonParser.parseReader(configReader).getAsJsonObject();

        boolean spaceBeforeColon = getBoolean(json, "declaration_space_before_colon", true);
        boolean spaceAfterColon = getBoolean(json, "declaration_space_after_colon", true);
        boolean spaceBeforeAssign = getBoolean(json, "assignment_space_before_equals", true);
        boolean spaceAfterAssign = getBoolean(json, "assignment_space_after_equals", true);
        int blankLinesBeforePrintln = getInt(json, "println_new_lines_before_call", 1);

        return new FormattingRules(spaceBeforeColon, spaceAfterColon, spaceBeforeAssign, spaceAfterAssign,
                Math.min(blankLinesBeforePrintln, 2));
    }

    private boolean getBoolean(JsonObject json, String key, boolean defaultValue) {
        return json.has(key) ? json.get(key).getAsBoolean() : defaultValue;
    }

    private int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) ? json.get(key).getAsInt() : defaultValue;
    }
}
