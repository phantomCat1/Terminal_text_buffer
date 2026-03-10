package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the {@link Cell} class.
 * <p>
 * Uses the real {@link CellAttributes} class with its fluent API.
 */
class CellTest {

    // --- Helper methods to create distinct CellAttributes instances ---

    private CellAttributes defaultAttributes() {
        return CellAttributes.DEFAULT;
    }

    private CellAttributes customAttributes() {
        return CellAttributes.DEFAULT
                .setForegroundColor(TerminalColor.RED)
                .setBold(true)
                .setItalic(true)
                .setUnderline(true);
    }

    private CellAttributes anotherCustomAttributes() {
        return CellAttributes.DEFAULT
                .setBackgroundColor(TerminalColor.BRIGHT_MAGENTA)
                .setBold(false)
                .setItalic(true)
                .setUnderline(false);
    }

    // --- Tests for constructors ---

    @Test
    void constructorWithCharacterAndAttributes_setsFieldsCorrectly() {
        CellAttributes attrs = customAttributes();
        Cell cell = new Cell('A', attrs);
        assertEquals('A', cell.getCharacter());
        assertSame(attrs, cell.getAttributes());
        assertFalse(cell.isEmpty());
    }

    @Test
    void constructorWithCharacterOnly_usesDefaultAttributes() {
        Cell cell = new Cell('B');
        assertEquals('B', cell.getCharacter());
        assertSame(CellAttributes.DEFAULT, cell.getAttributes());
        assertFalse(cell.isEmpty());
    }

    @Test
    void constructorWithCharacterAndNullAttributes_storesNull() {
        Cell cell = new Cell('C', null);
        assertEquals('C', cell.getCharacter());
        assertNull(cell.getAttributes());
    }

    // --- Tests for EMPTY constant ---

    @Test
    void emptyConstant_hasNullCharacterAndDefaultAttributes() {
        Cell empty = Cell.EMPTY;
        assertEquals('\0', empty.getCharacter());
        assertSame(CellAttributes.DEFAULT, empty.getAttributes());
        assertTrue(empty.isEmpty());
    }

    // --- Tests for isEmpty() ---

    @Test
    void isEmpty_returnsTrueOnlyWhenCharacterIsNullChar() {
        Cell cell1 = new Cell('\0', defaultAttributes());
        assertTrue(cell1.isEmpty());

        Cell cell2 = new Cell('X', defaultAttributes());
        assertFalse(cell2.isEmpty());

        // Character '\0' with non-default attributes is still empty.
        CellAttributes nonDefault = customAttributes();
        Cell cell3 = new Cell('\0', nonDefault);
        assertTrue(cell3.isEmpty());
    }

    // --- Tests for getters and setters ---

    @Test
    void setCharacter_updatesCharacter() {
        Cell cell = new Cell('A', defaultAttributes());
        cell.setCharacter('B');
        assertEquals('B', cell.getCharacter());
    }

    @Test
    void setCharacter_toNullChar_makesCellEmpty() {
        Cell cell = new Cell('A', defaultAttributes());
        assertFalse(cell.isEmpty());
        cell.setCharacter('\0');
        assertTrue(cell.isEmpty());
    }

    @Test
    void setAttributes_updatesAttributes() {
        Cell cell = new Cell('A', defaultAttributes());
        CellAttributes newAttrs = customAttributes();
        cell.setAttributes(newAttrs);
        assertSame(newAttrs, cell.getAttributes());
    }

    @Test
    void setAttributes_allowsNull() {
        Cell cell = new Cell('A', defaultAttributes());
        cell.setAttributes(null);
        assertNull(cell.getAttributes());
    }

    // --- Tests for equals() and hashCode() ---

    @Test
    void equals_sameObject_returnsTrue() {
        Cell cell = new Cell('A', defaultAttributes());
        assertEquals(cell, cell);
    }

    @Test
    void equals_equalFields_returnsTrue() {
        CellAttributes attrs = customAttributes();
        Cell cell1 = new Cell('A', attrs);
        Cell cell2 = new Cell('A', attrs);
        assertEquals(cell1, cell2);
        assertEquals(cell1.hashCode(), cell2.hashCode());
    }

    @Test
    void equals_differentCharacter_returnsFalse() {
        CellAttributes attrs = customAttributes();
        Cell cell1 = new Cell('A', attrs);
        Cell cell2 = new Cell('B', attrs);
        assertNotEquals(cell1, cell2);
    }

    @Test
    void equals_differentAttributes_returnsFalse() {
        CellAttributes attrs1 = customAttributes();
        CellAttributes attrs2 = anotherCustomAttributes();
        Cell cell1 = new Cell('A', attrs1);
        Cell cell2 = new Cell('A', attrs2);
        assertNotEquals(cell1, cell2);
    }

    @Test
    void equals_bothHaveNullAttributes_returnsTrue() {
        Cell cell1 = new Cell('A', null);
        Cell cell2 = new Cell('A', null);
        assertEquals(cell1, cell2);
        assertEquals(cell1.hashCode(), cell2.hashCode());
    }

    @Test
    void equals_oneHasNullAttributes_otherHasNonNull_returnsFalse() {
        Cell cell1 = new Cell('A', null);
        Cell cell2 = new Cell('A', defaultAttributes());
        assertNotEquals(cell2, cell1);
    }

    @Test
    void equals_withNonCellObject_returnsFalse() {
        Cell cell = new Cell('A', defaultAttributes());
        assertNotEquals(cell, "not a cell");
    }

    // --- Tests for toString() ---

    @Test
    void toString_emptyCell_returnsExpectedFormat() {
        Cell empty = Cell.EMPTY;
        assertEquals("Cell{EMPTY}", empty.toString());
    }

    @Test
    void toString_nonEmptyCell_returnsExpectedFormat() {
        CellAttributes attrs = customAttributes();
        Cell cell = new Cell('A', attrs);
        // The current toString implementation: "Cell{'" + character + ", " + attributes + "}"
        // Note: there's a missing closing brace after the character in the original code.
        // We'll test exactly what the code produces.
        String expected = "Cell{" + 'A' + ", " + attrs.toString() + "}";
        assertEquals(expected, cell.toString());
    }

    @Test
    void toString_withNullAttributes_handlesNull() {
        Cell cell = new Cell('A', null);

        assertEquals("Cell{A, null}", cell.toString());
    }

    // --- Additional test: ensure that Cell's attributes are not accidentally shared ---

    @Test
    void cellAndAttributesAreIndependentReferences() {
        CellAttributes originalAttrs = customAttributes();
        Cell cell = new Cell('A', originalAttrs);

        // Modify the original attributes object (if it were mutable; but CellAttributes is immutable,
        // so this test just verifies that the reference stored is the same).
        // Since CellAttributes is immutable, we can't change it, but we can check that the cell
        // doesn't get affected by creating a new attributes instance.
        CellAttributes newAttrs = originalAttrs.setBold(false);
        cell.setAttributes(newAttrs);
        assertSame(newAttrs, cell.getAttributes());
        assertNotSame(originalAttrs, cell.getAttributes());
    }
}