package printscript.formatter.spacing;

// Estado del formateo en el punto actual: lo que las reglas necesitan saber
// además de los dos tokens que están separando.
public record FormatContext(
        String originalGap, int depth, int indentSize, boolean previousStatementWasPrintln) {

    public String lineBreak(int lines) {
        return "\n".repeat(lines) + " ".repeat(Math.max(depth, 0) * indentSize);
    }
}
