package printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import printscript.ast.Statement;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.interpreter.handler.AssignmentHandler;
import printscript.interpreter.handler.HandlerRegistry;
import printscript.interpreter.handler.IfStatementHandler;
import printscript.interpreter.handler.PrintlnStatementHandler;
import printscript.interpreter.handler.StatementHandler;
import printscript.interpreter.handler.VariableDeclarationHandler;
import printscript.interpreter.runtime.ExpressionEvaluator;
import printscript.interpreter.runtime.FixedInputSource;
import printscript.interpreter.runtime.GlobalEnvironment;
import printscript.interpreter.runtime.InputSource;
import printscript.lexer.PrintScriptLexer;
import printscript.parser.AssignmentParser;
import printscript.parser.IfStatementParser;
import printscript.parser.PrecedenceClimbingExpressionParser;
import printscript.parser.PrintScriptParser;
import printscript.parser.PrintlnStatementParser;
import printscript.parser.StatementParser;
import printscript.parser.VariableDeclarationParser;

class PipelineIntegrationTest {

    private record RunResult(String output, boolean hadFailure) {}

    private RunResult run(String source) {
        return run(source, prompt -> prompt);
    }

    private RunResult run(String source, InputSource inputSource) {
        var lexer = new PrintScriptLexer(new StringReader(source));

        List<StatementParser> statementParsers = new ArrayList<>();
        statementParsers.add(new VariableDeclarationParser());
        statementParsers.add(new AssignmentParser());
        statementParsers.add(new PrintlnStatementParser());
        statementParsers.add(new IfStatementParser(() -> statementParsers));

        var parser =
                new PrintScriptParser(
                        lexer, statementParsers, new PrecedenceClimbingExpressionParser());

        var output = new ByteArrayOutputStream();
        var evaluator = new ExpressionEvaluator(inputSource);
        List<StatementHandler> statementHandlers = new ArrayList<>();
        HandlerRegistry registry = new HandlerRegistry(statementHandlers);
        statementHandlers.add(new VariableDeclarationHandler(evaluator));
        statementHandlers.add(new AssignmentHandler(evaluator));
        statementHandlers.add(new PrintlnStatementHandler(evaluator, new PrintStream(output)));
        statementHandlers.add(new IfStatementHandler(evaluator, () -> registry));

        var interpreter = new PrintScriptInterpreter(parser, new GlobalEnvironment(), registry);

        boolean hadFailure = false;
        while (interpreter.hasNext()) {
            Result<Statement> result = interpreter.next();
            hadFailure |=
                    switch (result) {
                        case Success<Statement> s -> false;
                        case Failure<Statement> f -> true;
                    };
        }

        return new RunResult(output.toString(StandardCharsets.UTF_8).strip(), hadFailure);
    }

    @Test
    void example1ConcatenatesStrings() {
        String source =
                """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """;

        RunResult result = run(source);

        assertEquals("Joe Doe", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void example2DividesNumbersAndConcatenatesResult() {
        String source =
                """
            let a: number = 12;
            let b: number = 4;
            let c: number = a / b;
            println("Result: " + c);
            """;

        RunResult result = run(source);

        assertEquals("Result: 3", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void example3ReassignsVariableAfterDivision() {
        String source =
                """
            let a: number = 12;
            let b: number = 4;
            a = a / b;
            println("Result: " + a);
            """;

        RunResult result = run(source);

        assertEquals("Result: 3", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void divisionByZeroStopsExecutionBeforePrinting() {
        String source =
                """
            let a: number = 10;
            let b: number = 0;
            let c: number = a / b;
            println(c);
            """;

        RunResult result = run(source);

        assertTrue(result.hadFailure());
        assertEquals("", result.output());
    }

    @Test
    void reportsTypeMismatchOnDeclaration() {
        String source = "let x: number = \"esto no es un numero\";";

        RunResult result = run(source);

        assertTrue(result.hadFailure());
    }

    @Test
    void reportsUndeclaredVariableInAssignment() {
        String source = "x = 5;";

        RunResult result = run(source);

        assertTrue(result.hadFailure());
    }

    @Test
    void reportsRedeclaration() {
        String source =
                """
            let x: number = 1;
            let x: string = "hola";
            """;

        RunResult result = run(source);

        assertTrue(result.hadFailure());
    }

    @Test
    void reportsArithmeticWithStringOperand() {
        String source = "println(\"hola\" - 1);";

        RunResult result = run(source);

        assertTrue(result.hadFailure());
    }

    @Test
    void reportsUninitializedVariableUsedBeforeAssignment() {
        String source = """
            let x: number;
            println(x);
            """;

        RunResult result = run(source);

        assertTrue(result.hadFailure());
        assertEquals("", result.output());
    }

    @Test
    void declaresWithoutInitializerThenAssignsWorksFine() {
        String source =
                """
            let x: number;
            x = 5;
            println(x);
            """;

        RunResult result = run(source);

        assertEquals("5", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void syntaxErrorRecoversAndContinuesToNextStatement() {
        String source = """
            let x number = 5;
            println(x);
            """;

        RunResult result = run(source);

        assertTrue(result.hadFailure());
    }

    @Test
    void ifTrueExecutesThenBranch() {
        String source =
                """
        let flag: boolean = true;
        if (flag) {
            println("dentro");
        }
        """;

        RunResult result = run(source);

        assertEquals("dentro", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void ifFalseWithElseExecutesElseBranch() {
        String source =
                """
        let flag: boolean = false;
        if (flag) {
            println("then");
        } else {
            println("else");
        }
        """;

        RunResult result = run(source);

        assertEquals("else", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void ifFalseWithoutElseExecutesNothing() {
        String source =
                """
        let flag: boolean = false;
        if (flag) {
            println("no debería imprimirse");
        }
        println("después");
        """;

        RunResult result = run(source);

        assertEquals("después", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void variableDeclaredInsideIfDoesNotLeakOutside() {
        String source =
                """
        let flag: boolean = true;
        if (flag) {
            let mensaje: string = "hola";
        }
        println(mensaje);
        """;

        RunResult result = run(source);

        assertTrue(result.hadFailure());
    }

    @Test
    void assignmentInsideIfAffectsOuterVariable() {
        String source =
                """
        let contador: number = 0;
        let flag: boolean = true;
        if (flag) {
            contador = 1;
        }
        println(contador);
        """;

        RunResult result = run(source);

        assertEquals("1", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void reportsNonBooleanCondition() {
        String source =
                """
        let flag: number = 1;
        if (flag) {
            println("no debería llegar acá");
        }
        """;

        RunResult result = run(source);

        assertTrue(result.hadFailure());
        assertEquals("", result.output());
    }

    @Test
    void readInputCoercesToDeclaredNumberType() {
        String source =
                """
        let edad: number = readInput("Edad:");
        println(edad + 1);
        """;

        RunResult result = run(source, new FixedInputSource(List.of("42")));

        assertEquals("43", result.output());
        assertFalse(result.hadFailure());
    }

    @Test
    void readInputFailsWhenValueCannotBeCoercedToDeclaredType() {
        String source = "let edad: number = readInput(\"Edad:\");";

        RunResult result = run(source, new FixedInputSource(List.of("no es un numero")));

        assertTrue(result.hadFailure());
    }
}
