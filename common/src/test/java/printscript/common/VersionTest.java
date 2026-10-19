package printscript.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void parsesKnownLabels() {
        assertEquals(Version.V1_0, Version.from("1.0"));
        assertEquals(Version.V1_1, Version.from("1.1"));
    }

    @Test
    void rejectsUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> Version.from("2.0"));
    }

    @Test
    void latestIsVersionOneDotOne() {
        assertEquals(Version.V1_1, Version.latest());
        assertEquals("1.1", Version.latest().label());
    }
}
