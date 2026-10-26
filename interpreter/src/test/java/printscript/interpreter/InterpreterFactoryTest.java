package printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import printscript.ast.BooleanLiteral;
import printscript.ast.IfStatement;
import printscript.ast.PrintlnStatement;
import printscript.ast.Statement;
import printscript.ast.StringLiteral;
import printscript.common.Version;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;

class InterpreterFactoryTest {

    private final Span span = Span.of(new Position(0, 0), new Position(0, 1));
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private Result<Statement> runIfThatPrints(Version version) {
        Statement println = new PrintlnStatement(new StringLiteral("adentro", span), span);
        Statement ifStatement =
                new IfStatement(
                        new BooleanLiteral(true, span), List.of(println), Optional.empty(), span);
        List<Result<Statement>> statements = List.of(Result.success(ifStatement));

        var interpreter =
                InterpreterFactory.create(
                        statements.iterator(),
                        version,
                        new PrintStream(output, true, StandardCharsets.UTF_8),
                        prompt -> prompt);
        return interpreter.next();
    }

    @Test
    void versionOneDotOneExecutesIf() {
        assertInstanceOf(Success.class, runIfThatPrints(Version.V1_1));
        assertEquals("adentro", output.toString(StandardCharsets.UTF_8).strip());
    }

    @Test
    void versionOneDotZeroHasNoIfHandler() {
        assertInstanceOf(Failure.class, runIfThatPrints(Version.V1_0));
        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }
}
