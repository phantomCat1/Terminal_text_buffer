package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {
    @Test
    @DisplayName("Invalid width throws")
    void invalidWidth() {
        assertThrows(IllegalArgumentException.class, () -> new Line(0));
    }

    @Test
    @DisplayName("setCell at out-of-range column throws")
    void outOfRangeSetCell() {
        Line line = new Line(5);
        assertThrows(IndexOutOfBoundsException.class, () -> line.setCell(5, Cell.EMPTY));
    }


        // Minimal TerminalColor enum – replace with actual if available.
        private enum TerminalColor {
            BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE
        }

        // --- Helper methods to create Cell objects with various attributes ---

        private Cell createCell(char ch, boolean bold, boolean italic, boolean underline) {
            CellAttributes attrs = CellAttributes.DEFAULT
                    .setBold(bold)
                    .setItalic(italic)
                    .setUnderline(underline);
            return new Cell(ch, attrs);
        }

        private Cell cellA() {
            return createCell('A', false, false, false);
        }

        private Cell cellB() {
            return createCell('B', true, false, true);
        }

        private Cell cellEmpty() {
            return Cell.EMPTY;
        }

        // ==================== Constructor: Line(int width, Cell cell) ====================

        @Test
        void constructorWithWidthAndCell_negativeWidth_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Line(-1, cellA()),
                    "Width must be positive, got: -1");
        }

        @Test
        void constructorWithWidthAndCell_zeroWidth_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Line(0, cellA()),
                    "Width must be positive, got: 0");
        }

        @Test
        void constructorWithWidthAndCell_nullCell_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Line(5, null),
                    "Cell is null");
        }


        // ==================== Constructor: Line(int width) ====================

        @Test
        void constructorWithWidthOnly_negativeWidth_throwsException() {
            // This constructor delegates to the first one, so it should throw.
            assertThrows(IllegalArgumentException.class,
                    () -> new Line(-1));
        }


        // ==================== Constructor: Line(List<Cell> cellList) ====================

        @Test
        void constructorWithList_nullList_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () -> new Line((List<Cell>) null));
        }

        @Test
        void constructorWithList_emptyList_createsEmptyLine() {
            List<Cell> emptyList = new ArrayList<>();
            Line line = new Line(emptyList);
            assertEquals(0, line.getWidth());
        }

        @Test
        void constructorWithList_nonEmptyList_createsDeepCopy() {
            List<Cell> original = new ArrayList<>();
            original.add(cellA());
            original.add(cellB());

            Line line = new Line(original);

            assertEquals(2, line.getWidth());

            // Verify cells are independent copies (not the same references)
            assertNotSame(original.get(0), line.getCell(0));
            assertNotSame(original.get(1), line.getCell(1));

            // But content should be equal
            assertEquals(original.get(0), line.getCell(0));
            assertEquals(original.get(1), line.getCell(1));
        }

        @Test
        void constructorWithList_deepCopy_independentModifications() {
            List<Cell> original = new ArrayList<>();
            Cell originalCell = new Cell('X', CellAttributes.DEFAULT);
            original.add(originalCell);

            Line line = new Line(original);
            Cell lineCell = line.getCell(0);

            // Modify original cell – should not affect line's cell
            originalCell.setCharacter('Y');
            originalCell.setAttributes(CellAttributes.DEFAULT.setBold(true));
            assertEquals('X', lineCell.getCharacter());
            assertEquals(CellAttributes.DEFAULT, lineCell.getAttributes());

            // Modify line's cell – should not affect original
            lineCell.setCharacter('Z');
            lineCell.setAttributes(CellAttributes.DEFAULT.setItalic(true));
            assertEquals('Y', originalCell.getCharacter());
            assertTrue(originalCell.getAttributes().isBold());
            assertFalse(originalCell.getAttributes().isItalic());
        }

        // ==================== getWidth() ====================

        @Test
        void getWidth_returnsCorrectSize() {
            List<Cell> list = List.of(cellA(), cellB());
            Line line = new Line(list);
            assertEquals(2, line.getWidth());

        }

        // ==================== getCell() ====================

        @Test
        void getCell_validIndex_returnsCell() {
            List<Cell> list = List.of(cellA(), cellB());
            Line line = new Line(list);

            Cell cell0 = line.getCell(0);
            Cell cell1 = line.getCell(1);

            assertNotNull(cell0);
            assertNotNull(cell1);
            assertEquals('A', cell0.getCharacter());
            assertEquals('B', cell1.getCharacter());
        }

        @Test
        void getCell_invalidIndex_lowerBound_throwsException() {
            List<Cell> list = List.of(cellA());
            Line line = new Line(list);

            IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class,
                    () -> line.getCell(-1));
            assertEquals("Column out of bounds", ex.getMessage());
        }

        @Test
        void getCell_invalidIndex_upperBound_throwsException() {
            List<Cell> list = List.of(cellA());
            Line line = new Line(list);

            IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class,
                    () -> line.getCell(1));
            assertEquals("Column out of bounds", ex.getMessage());
        }

        // ==================== setCell() ====================

        @Test
        void setCell_validIndex_setsCell() {
            List<Cell> list = List.of(cellA(), cellB());
            Line line = new Line(list);

            Cell newCell = new Cell('C', CellAttributes.DEFAULT.setUnderline(true));
            line.setCell(1, newCell);

            assertSame(newCell, line.getCell(1), "The exact cell object should be stored");
            assertEquals('C', line.getCell(1).getCharacter());
            assertTrue(line.getCell(1).getAttributes().isUnderline());
        }

        @Test
        void setCell_invalidIndex_throwsException() {
            List<Cell> list = List.of(cellA());
            Line line = new Line(list);

            IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class,
                    () -> line.setCell(1, cellB()));
            assertEquals("Column out of bounds", ex.getMessage());
        }

        // ==================== fill() ====================


        @Test
        void fill_nonEmptyLine_updatesAllCells() {
            List<Cell> list = new ArrayList<>();
            list.add(new Cell('1', CellAttributes.DEFAULT));
            list.add(new Cell('2', CellAttributes.DEFAULT.setBold(true)));
            Line line = new Line(list);

            Cell fillCell = new Cell('F', CellAttributes.DEFAULT.setItalic(true));
            line.fill(fillCell);

            for (int i = 0; i < line.getWidth(); i++) {
                Cell c = line.getCell(i);
                assertEquals('F', c.getCharacter());
                assertTrue(c.getAttributes().isItalic());
                // Other attributes should be exactly those of fillCell
                assertEquals(fillCell.getAttributes(), c.getAttributes());
            }
        }

        @Test
        void fill_doesNotReplaceCells_justUpdates() {
            List<Cell> list = new ArrayList<>();
            Cell originalCell1 = new Cell('1', CellAttributes.DEFAULT);
            Cell originalCell2 = new Cell('2', CellAttributes.DEFAULT.setBold(true));
            list.add(originalCell1);
            list.add(originalCell2);
            Line line = new Line(list);

            // Capture references before fill
            Cell ref1 = line.getCell(0);
            Cell ref2 = line.getCell(1);

            line.fill(cellA());

            // References should be the same objects
            assertSame(ref1, line.getCell(0));
            assertSame(ref2, line.getCell(1));
            // But their content changed
            assertEquals('A', ref1.getCharacter());
            assertEquals('A', ref2.getCharacter());
        }

        // ==================== toString() ====================

        @Test
        void toString_emptyLine_returnsEmptyBrackets() {
            Line emptyLine = new Line(5); // empty due to bug
            assertEquals("Line[     ]", emptyLine.toString());
        }

        @Test
        void toString_withCells_includesCharacters() {
            List<Cell> list = List.of(
                    new Cell('H', CellAttributes.DEFAULT),
                    new Cell('i', CellAttributes.DEFAULT),
                    Cell.EMPTY
            );
            Line line = new Line(list);
            // EMPTY cell should be represented as a space
            assertEquals("Line[Hi ]", line.toString());
        }

        @Test
        void toString_withAllEmptyCells_returnsSpaces() {
            List<Cell> list = List.of(Cell.EMPTY, Cell.EMPTY, Cell.EMPTY);
            Line line = new Line(list);
            assertEquals("Line[   ]", line.toString());
        }

    @Test
    void constructorWithWidthAndCell_fixed_shouldPopulateList() {
        // When the bug is fixed, this test should pass.
        Cell template = cellA();
        Line line = new Line(3, template);
        assertEquals(3, line.getWidth());
        for (int i = 0; i < 3; i++) {
            Cell c = line.getCell(i);
            assertEquals(template.getCharacter(), c.getCharacter());
            assertEquals(template.getAttributes(), c.getAttributes());
            assertNotSame(template, c); // Should be copies
        }
    }

    @Test
    void constructorWithWidthOnly_fixed_shouldPopulateWithEmptyCells() {
        Line line = new Line(3);
        assertEquals(3, line.getWidth());
        for (int i = 0; i < 3; i++) {
            assertTrue(line.getCell(i).isEmpty());
            assertEquals(CellAttributes.DEFAULT, line.getCell(i).getAttributes());
        }
    }



}