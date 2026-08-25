package printscript.ast;

import org.junit.jupiter.api.Test;
import printscript.common.token.Position;
import printscript.common.token.Span;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AstTest {

    private final Span span = Span.of(new Position(1, 0), new Position(1, 5));

    @Test
    void recordsWithSameValuesAreEqual() {
        var a = new NumberLiteral(BigDecimal.valueOf(5), span);
        var b = new NumberLiteral(BigDecimal.valueOf(5), span);

        assertEquals(a, b);
    }

    @Test
    void buildsTreeForTwoPlusThree() {
        var left = new NumberLiteral(BigDecimal.valueOf(2), span);
        var right = new NumberLiteral(BigDecimal.valueOf(3), span);
        var sum = new BinaryExpression(left, BinaryOperator.PLUS, right, span);

        assertEquals(left, sum.left());
        assertEquals(right, sum.right());
        assertEquals(BinaryOperator.PLUS, sum.operator());
    }

    @Test
    void variableDeclarationWithoutInitializerHasEmptyOptional() {
        var declaration = new VariableDeclaration("x", DeclaredType.NUMBER, Optional.empty(), span);

        assertTrue(declaration.initializer().isEmpty());
    }

    // demuestra que el switch exhaustivo sobre Expression compila
    private String describe(Expression expression) {
        return switch (expression) {
            case NumberLiteral n -> "number:" + n.value();
            case StringLiteral s -> "string:" + s.value();
            case Identifier id -> "identifier:" + id.name();
            case BinaryExpression b -> "binary:" + b.operator();
        };
    }

    @Test
    void switchOverExpressionIsExhaustive() {
        Expression number = new NumberLiteral(BigDecimal.TEN, span);

        assertEquals("number:10", describe(number));
    }
    @Test
    void identifierHoldsItsName() {
        var identifier = new Identifier("x", span);

        assertEquals("x", identifier.name());
    }

    @Test
    void stringLiteralHoldsItsValue() {
        var literal = new StringLiteral("hola", span);

        assertEquals("hola", literal.value());
    }

    @Test
    void assignmentHoldsNameAndValue() {
        var value = new NumberLiteral(BigDecimal.ONE, span);
        var assignment = new Assignment("x", value, span);

        assertEquals("x", assignment.name());
        assertEquals(value, assignment.value());
    }

    @Test
    void printlnStatementHoldsItsArgument() {
        var argument = new Identifier("x", span);
        var println = new PrintlnStatement(argument, span);

        assertEquals(argument, println.argument());
    }
    @Test
    void switchOverExpressionCoversStringAndIdentifier() {
        assertEquals("string:hola", describe(new StringLiteral("hola", span)));
        assertEquals("identifier:x", describe(new Identifier("x", span)));
    }
}