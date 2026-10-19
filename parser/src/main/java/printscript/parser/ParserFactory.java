package printscript.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import printscript.common.Version;
import printscript.common.result.Result;
import printscript.common.token.Token;

// Único lugar que sabe qué sentencias se pueden parsear en cada versión del lenguaje.
public final class ParserFactory {

    private ParserFactory() {}

    public static PrintScriptParser create(Iterator<Result<Token>> tokens, Version version) {
        List<StatementParser> parsers = new ArrayList<>();
        parsers.add(new VariableDeclarationParser());
        parsers.add(new AssignmentParser());
        parsers.add(new PrintlnStatementParser());

        List<StatementParser> extras =
                switch (version) {
                    case V1_0 -> List.of();
                    case V1_1 -> List.of(new IfStatementParser(() -> parsers));
                };
        parsers.addAll(extras);

        return new PrintScriptParser(tokens, parsers, new PrecedenceClimbingExpressionParser());
    }
}
