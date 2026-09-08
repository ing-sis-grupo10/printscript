package printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.BinaryExpression;
import printscript.ast.BinaryOperator;
import printscript.ast.Expression;
import printscript.ast.NumberLiteral;
import printscript.ast.StringLiteral;
import printscript.common.result.Result;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;

class PrecedenceClimbingExpressionParserTest {

    private final PrecedenceClimbingExpressionParser parser =
            new PrecedenceClimbingExpressionParser();

    private Span span(String value) {
        return Span.of(new Position(1, 0), new Position(1, value.length()));
    }

    private TokenStream streamOf(Token... tokens) {
        List<Result<Token>> results = new ArrayList<>();
        for (Token t : tokens) {
            results.add(Result.success(t));
        }
        return new TokenStream(results.iterator());
    }

    @Test
    void parsesStringLiteralFactor() {
        TokenStream tokens = streamOf(new Token(TokenType.STRING_LITERAL, "hola", span("hola")));

        Expression result = parser.parseExpression(tokens);

        assertInstanceOf(StringLiteral.class, result);
        assertEquals("hola", ((StringLiteral) result).value());
    }

    @Test
    void parsesMultiplicationWithHigherPrecedenceThanAddition() {
        // 2 + 3 * 4 debería agrupar como 2 + (3 * 4)
        TokenStream tokens =
                streamOf(
                        new Token(TokenType.NUMBER_LITERAL, "2", span("2")),
                        new Token(TokenType.PLUS, "+", span("+")),
                        new Token(TokenType.NUMBER_LITERAL, "3", span("3")),
                        new Token(TokenType.STAR, "*", span("*")),
                        new Token(TokenType.NUMBER_LITERAL, "4", span("4")));

        Expression result = parser.parseExpression(tokens);

        assertInstanceOf(BinaryExpression.class, result);
        var outer = (BinaryExpression) result;
        assertEquals(BinaryOperator.PLUS, outer.operator());
        assertInstanceOf(NumberLiteral.class, outer.left());
        assertInstanceOf(BinaryExpression.class, outer.right());
        assertEquals(BinaryOperator.TIMES, ((BinaryExpression) outer.right()).operator());
    }

    @Test
    void parsesDivision() {
        TokenStream tokens =
                streamOf(
                        new Token(TokenType.NUMBER_LITERAL, "10", span("10")),
                        new Token(TokenType.SLASH, "/", span("/")),
                        new Token(TokenType.NUMBER_LITERAL, "2", span("2")));

        Expression result = parser.parseExpression(tokens);

        assertInstanceOf(BinaryExpression.class, result);
        assertEquals(BinaryOperator.DIVIDE, ((BinaryExpression) result).operator());
    }

    @Test
    void parsesSubtraction() {
        TokenStream tokens =
                streamOf(
                        new Token(TokenType.NUMBER_LITERAL, "10", span("10")),
                        new Token(TokenType.MINUS, "-", span("-")),
                        new Token(TokenType.NUMBER_LITERAL, "2", span("2")));

        Expression result = parser.parseExpression(tokens);

        assertInstanceOf(BinaryExpression.class, result);
        assertEquals(BinaryOperator.MINUS, ((BinaryExpression) result).operator());
    }

    @Test
    void parsesNegativeNumberLiteral() {
        TokenStream tokens =
                streamOf(
                        new Token(TokenType.MINUS, "-", span("-")),
                        new Token(TokenType.NUMBER_LITERAL, "5", span("5")));

        Expression result = parser.parseExpression(tokens);

        assertInstanceOf(NumberLiteral.class, result);
        assertEquals(new BigDecimal("-5"), ((NumberLiteral) result).value());
    }

    @Test
    void parsesAdditionWithNegativeNumberOperand() {
        // 5 + -3
        TokenStream tokens =
                streamOf(
                        new Token(TokenType.NUMBER_LITERAL, "5", span("5")),
                        new Token(TokenType.PLUS, "+", span("+")),
                        new Token(TokenType.MINUS, "-", span("-")),
                        new Token(TokenType.NUMBER_LITERAL, "3", span("3")));

        Expression result = parser.parseExpression(tokens);

        assertInstanceOf(BinaryExpression.class, result);
        var binary = (BinaryExpression) result;
        assertInstanceOf(NumberLiteral.class, binary.right());
        assertEquals(new BigDecimal("-3"), ((NumberLiteral) binary.right()).value());
    }

    @Test
    void unaryMinusNotFollowedByNumberIsAParseError() {
        TokenStream tokens =
                streamOf(
                        new Token(TokenType.MINUS, "-", span("-")),
                        new Token(TokenType.IDENTIFIER, "x", span("x")));

        assertThrows(ParseError.class, () -> parser.parseExpression(tokens));
    }

    @Test
    void unrecognizedTokenIsAParseError() {
        TokenStream tokens = streamOf(new Token(TokenType.COLON, ":", span(":")));

        assertThrows(ParseError.class, () -> parser.parseExpression(tokens));
    }
}
