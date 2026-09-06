package printscript.analyzer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;

// traduce JSON a AnalyzerRules
public final class AnalyzerRulesLoader {

    public AnalyzerRules load(Reader configReader) {
        JsonObject json = JsonParser.parseReader(configReader).getAsJsonObject();

        AnalyzerRules.IdentifierCase identifierCase =
                json.has("identifier_format")
                                && "snake_case"
                                        .equalsIgnoreCase(
                                                json.get("identifier_format").getAsString())
                        ? AnalyzerRules.IdentifierCase.SNAKE_CASE
                        : AnalyzerRules.IdentifierCase.CAMEL_CASE;

        boolean printlnRestricted =
                !json.has("println_identifier_or_literal_only")
                        || json.get("println_identifier_or_literal_only").getAsBoolean();

        return new AnalyzerRules(identifierCase, printlnRestricted);
    }
}
