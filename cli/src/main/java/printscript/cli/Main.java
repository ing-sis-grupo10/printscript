package printscript.cli;

import java.io.IOException;

public final class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Uso: printscript <Validation|Execution|Formatting|Analyzing> <archivo> [--config <archivo>] [--version 1.0]");
            System.exit(2);
        }

        String operation = args[0];
        String sourceFile = args[1];
        String configFile = null;
        String version = "1.0";

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "--config" -> configFile = args[++i];
                case "--version" -> version = args[++i];
                default -> {
                    System.err.println("Argumento desconocido: " + args[i]);
                    System.exit(2);
                }
            }
        }

        if (!"1.0".equals(version)) {
            System.err.println("Versión no soportada: " + version);
            System.exit(1);
        }

        Pipeline pipeline = new Pipeline(sourceFile, configFile);
        int exitCode;
        try {
            exitCode =
                    switch (operation) {
                        case "Validation" -> pipeline.validate();
                        case "Execution" -> pipeline.execute();
                        case "Formatting" -> pipeline.format();
                        case "Analyzing" -> pipeline.analyze();
                        default -> {
                            System.err.println("Operación desconocida: " + operation);
                            yield 2;
                        }
                    };
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
            exitCode = 2;
        }

        System.exit(exitCode);
    }
}
