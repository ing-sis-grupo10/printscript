package printscript.common.token;

public record Span(Position start, Position end) {
    public static Span of(Position start, Position end) {
        return new Span(start, end);
    }

    // útil para nodos compuestos: el span de "a + b" es el de "a" fusionado con el de "b"
    public static Span merge(Span first, Span second) {
        return new Span(first.start(), second.end());
    }
}