package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalColorTest {
    @Test
    @DisplayName("fromIndex round-trips for all colors")
    void fromIndexRoundTrip() {
        for (TerminalColor c : TerminalColor.values()) {
            assertEquals(c, TerminalColor.fromIndex(c.getIndex()));
        }
    }

    @Test
    @DisplayName("fromIndex throws for invalid index")
    void fromIndexInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TerminalColor.fromIndex(16));
        assertThrows(IllegalArgumentException.class, () -> TerminalColor.fromIndex(-1));
    }

    @Test
    @DisplayName("16 colors total")
    void sixteenColors() {
        assertEquals(16, TerminalColor.values().length);
    }

}