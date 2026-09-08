package printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PipelineTest {

    @TempDir Path tempDir;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void redirectStreams() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, false, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(err, false, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private String writeSource(String content) throws IOException {
        Path file = tempDir.resolve("program.prs");
        Files.writeString(file, content);
        return file.toString();
    }

    @Test
    void validationSucceedsOnWellTypedProgramWithoutRunningIt() throws IOException {
        String path =
                writeSource(
                        """
            let x: number = 5;
            println(x);
            """);

        int exitCode = new Pipeline(path, null).validate();

        assertEquals(0, exitCode);
        assertTrue(
                out.toString(StandardCharsets.UTF_8)
                        .isEmpty()); // Validation nunca debe mostrar output del programa
    }

    @Test
    void validationFailsOnTypeMismatchAndReportsLocation() throws IOException {
        String path = writeSource("let x: number = \"no es un numero\";");

        int exitCode = new Pipeline(path, null).validate();

        assertEquals(1, exitCode);
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("ERROR"));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("No se puede asignar"));
    }

    @Test
    void executionPrintsRealProgramOutput() throws IOException {
        String path =
                writeSource(
                        """
            let name: string = "Joe";
            println(name);
            """);

        int exitCode = new Pipeline(path, null).execute();

        assertEquals(0, exitCode);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("Joe"));
    }

    @Test
    void formattingRewritesSourceAccordingToDefaultRules() throws IOException {
        String path = writeSource("let  x:number=5;");

        int exitCode = new Pipeline(path, null).format();

        assertEquals(0, exitCode);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("let x : number = 5;"));
    }

    @Test
    void analyzingReportsNamingConventionWarningButStillSucceeds() throws IOException {
        String path = writeSource("let My_Var: number = 1;");

        int exitCode = new Pipeline(path, null).analyze();

        assertEquals(0, exitCode); // es un warning, no un error — no bloquea
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("WARNING"));
    }

    @Test
    void analyzingWithConfigDisablesPrintlnRule() throws IOException {
        String path = writeSource("println(1 + 2);");
        Path config = tempDir.resolve("analyzer.json");
        Files.writeString(config, "{\"println_identifier_or_literal_only\": false}");

        int exitCode = new Pipeline(path, config.toString()).analyze();

        assertEquals(0, exitCode);
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("WARNING"));
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("ERROR"));
    }
}
