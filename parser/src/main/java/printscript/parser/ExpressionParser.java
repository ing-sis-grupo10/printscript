package printscript.parser;

import printscript.ast.Expression;
import printscript.diagnostics.DiagnosticReporter;

public interface ExpressionParser {
    Expression parseExpression(TokenStream tokens, DiagnosticReporter reporter);
}