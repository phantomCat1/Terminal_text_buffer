package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellAttributesTest {
    @Test
    @DisplayName("DEFAULT has no colors and no styles")
    void defaultIsAllDefault() {
        CellAttributes d = CellAttributes.DEFAULT;
        assertNull(d.getForegroundColor());
        assertNull(d.getBackgroundColor());
        assertFalse(d.isBold());
        assertFalse(d.isItalic());
        assertFalse(d.isUnderline());
    }

    @Test
    @DisplayName("Fluent builder creates new instance")
    void fluentBuilder() {
        CellAttributes a = CellAttributes.DEFAULT
                .setForegroundColor(TerminalColor.RED)
                .setBold(true);
        assertEquals(TerminalColor.RED, a.getForegroundColor());
        assertTrue(a.isBold());
        // Original unchanged
        assertNull(CellAttributes.DEFAULT.getForegroundColor());
    }

    @Test
    @DisplayName("equals and hashCode are consistent")
    void equalsHashCode() {
        CellAttributes a1 = new CellAttributes(TerminalColor.BLUE, null, true, false, false);
        CellAttributes a2 = new CellAttributes(TerminalColor.BLUE, null, true, false, false);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        CellAttributes a3 = new CellAttributes(TerminalColor.RED, null, true, false, false);
        assertNotEquals(a1, a3);
    }

}