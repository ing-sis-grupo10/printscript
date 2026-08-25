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
import printscript.diagnostics.Diagnostic;
import printscript.diagnostics.DiagnosticReporter;

import java.math.BigDecimal;

public final class PrecedenceClimbingExpressionParser implements ExpressionParser {

    @Override
    public Expression parseExpression(TokenStream tokens, DiagnosticReporter reporter) {
        Expression left = parseTerm(tokens, reporter);

        while (isAdditive(tokens.peek().type())) {
            Token operatorToken = tokens.consume();
            Expression right = parseTerm(tokens, reporter);
            left = new BinaryExpression(left, toOperator(operatorToken.type()), right,
                    Span.merge(left.span(), right.span()));
        }

        return left;
    }

    private Expression parseTerm(TokenStream tokens, DiagnosticReporter reporter) {
        Expression left = parseFactor(tokens, reporter);

        while (isMultiplicative(tokens.peek().type())) {
            Token operatorToken = tokens.consume();
            Expression right = parseFactor(tokens, reporter);
            left = new BinaryExpression(left, toOperator(operatorToken.type()), right,
                    Span.merge(left.span(), right.span()));
        }

        return left;
    }

    private Expression parseFactor(TokenStream tokens, DiagnosticReporter reporter) {
        Token token = tokens.consume();

        return switch (token.type()) {
            case NUMBER_LITERAL -> new NumberLiteral(new BigDecimal(token.value()), token.span());
            case STRING_LITERAL -> new StringLiteral(token.value(), token.span());
            case IDENTIFIER -> new Identifier(token.value(), token.span());
            default -> {
                reporter.report(Diagnostic.error("Se esperaba un número, string o identificador", token.span()));
                yield new NumberLiteral(BigDecimal.ZERO, token.span());
            }
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
