package printscript.parser;

import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.ast.BinaryExpression;
import printscript.ast.BinaryOperator;
import printscript.ast.Expression;
import printscript.ast.Identifier;
import printscript.ast.NumberLiteral;
import printscript.ast.StringLiteral;

import java.math.BigDecimal;

public final class PrecedenceClimbingExpressionParser implements ExpressionParser {

    @Override
    public Expression parseExpression(TokenStream tokens) {
        Expression left = parseTerm(tokens);
        while (isAdditive(tokens.peek().type())) {
            Token operatorToken = tokens.consume();
            Expression right = parseTerm(tokens);
            left = new BinaryExpression(left, toOperator(operatorToken.type()), right,
                    Span.merge(left.span(), right.span()));
        }
        return left;
    }

    private Expression parseTerm(TokenStream tokens) {
        Expression left = parseFactor(tokens);
        while (isMultiplicative(tokens.peek().type())) {
            Token operatorToken = tokens.consume();
            Expression right = parseFactor(tokens);
            left = new BinaryExpression(left, toOperator(operatorToken.type()), right,
                    Span.merge(left.span(), right.span()));
        }
        return left;
    }

    private Expression parseFactor(TokenStream tokens) {
        Token token = tokens.consume();
        return switch (token.type()) {
            case NUMBER_LITERAL -> new NumberLiteral(new BigDecimal(token.value()), token.span());
            case STRING_LITERAL -> new StringLiteral(token.value(), token.span());
            case IDENTIFIER -> new Identifier(token.value(), token.span());
            default -> throw new ParseError("Se esperaba un número, string o identificador", token.span());
        };
    }

    private boolean isAdditive(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS;
    }

    private boolean isMultiplicative(TokenType type) {
        return type == TokenType.STAR || type == TokenType.SLASH;
    }

    private BinaryOperator toOperator(TokenType type) {
        return switch (type) {
            case PLUS -> BinaryOperator.PLUS;
            case MINUS -> BinaryOperator.MINUS;
            case STAR -> BinaryOperator.TIMES;
            case SLASH -> BinaryOperator.DIVIDE;
            default -> throw new IllegalStateException("no debería llegar acá");
        };
    }
}