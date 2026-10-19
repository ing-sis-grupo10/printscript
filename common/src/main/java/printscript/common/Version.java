package printscript.common;

public enum Version {
    V1_0("1.0"),
    V1_1("1.1");

    private final String label;

    Version(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Version latest() {
        return V1_1;
    }

    public static Version from(String label) {
        for (Version version : values()) {
            if (version.label.equals(label)) {
                return version;
            }
        }
        throw new IllegalArgumentException("Versión no soportada: " + label);
    }
}
