package printscript.parser;

import java.util.Optional;
import printscript.ast.DeclaredType;
import printscript.ast.Expression;
import printscript.ast.Statement;
import printscript.ast.VariableDeclaration;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

public final class VariableDeclarationParser implements StatementParser {

    @Override
    public boolean canParse(TokenStream tokens) {
        return tokens.peek().type() == TokenType.LET;
    }

    @Override
    public Statement parse(TokenStream tokens, ExpressionParser expressionParser) {
        Token letToken = tokens.consume();
        Token nameToken = tokens.expect(TokenType.IDENTIFIER);
        tokens.expect(TokenType.COLON);
        Token typeToken = tokens.consume();
        DeclaredType declaredType = parseDeclaredType(typeToken);

        Optional<Expression> initializer = Optional.empty();
        if (tokens.peek().type() == TokenType.ASSIGN) {
            tokens.consume();
            initializer = Optional.of(expressionParser.parseExpression(tokens));
        }

        Token semicolon = tokens.expect(TokenType.SEMICOLON);
        Span span = Span.merge(letToken.span(), semicolon.span());

        return new VariableDeclaration(nameToken.value(), declaredType, initializer, span);
    }

    private DeclaredType parseDeclaredType(Token typeToken) {
        return switch (typeToken.type()) {
            case NUMBER_TYPE -> DeclaredType.NUMBER;
            case STRING_TYPE -> DeclaredType.STRING;
            default ->
                    throw new ParseError(
                            "Tipo desconocido: " + typeToken.value(), typeToken.span());
        };
    }
}
