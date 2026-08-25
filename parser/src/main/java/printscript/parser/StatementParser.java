package printscript.parser;

import printscript.ast.Statement;
import printscript.diagnostics.DiagnosticReporter;

public interface StatementParser {
    boolean canParse(TokenStream tokens);
    Statement parse(TokenStream tokens, ExpressionParser expressionParser, DiagnosticReporter reporter);
}
