package printscript.parser;

import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.ast.DeclaredType;
import printscript.ast.Expression;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.diagnostics.Diagnostic;
import printscript.diagnostics.DiagnosticReporter;

import java.util.Optional;

public final class VariableDeclarationParser implements StatementParser {

    @Override
    public boolean canParse(TokenStream tokens) {
        return tokens.peek().type() == TokenType.LET;
    }

    @Override
    public Statement parse(TokenStream tokens, ExpressionParser expressionParser, DiagnosticReporter reporter) {
        Token letToken = tokens.consume();                    // LET
        Token nameToken = tokens.expect(TokenType.IDENTIFIER);
        tokens.expect(TokenType.COLON);
        Token typeToken = tokens.consume();                   // NUMBER_TYPE o STRING_TYPE
        DeclaredType declaredType = parseDeclaredType(typeToken, reporter);

        Optional<Expression> initializer = Optional.empty();
        if (tokens.peek().type() == TokenType.ASSIGN) {
            tokens.consume(); // ASSIGN
            initializer = Optional.of(expressionParser.parseExpression(tokens, reporter));
        }

        Token semicolon = tokens.expect(TokenType.SEMICOLON);
        Span span = Span.merge(letToken.span(), semicolon.span());

        return new VariableDeclaration(nameToken.value(), declaredType, initializer, span);
    }

    private DeclaredType parseDeclaredType(Token typeToken, DiagnosticReporter reporter) {
        return switch (typeToken.type()) {
            case NUMBER_TYPE -> DeclaredType.NUMBER;
            case STRING_TYPE -> DeclaredType.STRING;
            default -> {
                reporter.report(Diagnostic.error("Tipo desconocido: " + typeToken.value(), typeToken.span()));
                yield DeclaredType.NUMBER;
            }
        };
    }
}