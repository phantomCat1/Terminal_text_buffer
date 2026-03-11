package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {
    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("Default construction creates empty screen")
        void defaultConstruction() {
            TerminalBuffer buf = new TerminalBuffer(80, 24);
            assertEquals(80, buf.getWidth());
            assertEquals(24, buf.getHeight());
            assertEquals(0, buf.getCursorColumn());
            assertEquals(0, buf.getCursorRow());
            assertEquals(0, buf.getScrollbackSize());
        }

        @Test
        @DisplayName("Custom scrollback limit is respected")
        void customScrollback() {
            TerminalBuffer buf = new TerminalBuffer(80, 24, 500);
            assertEquals(500, buf.getMaxScrollback());
        }

        @Test
        @DisplayName("Invalid dimensions throw IllegalArgumentException")
        void invalidDimensions() {
            assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 24));
            assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 0));
            assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 24, -1));
        }

        @Test
        @DisplayName("Screen starts blank (spaces)")
        void screenStartsBlank() {
            TerminalBuffer buf = new TerminalBuffer(5, 3);

            assertEquals("     \n     \n     \n", buf.getScreenString());
        }
    }

    @Nested
    @DisplayName("Attributes")
    class AttributeTests {

        TerminalBuffer buf;

        @BeforeEach
        void setUp() { buf = new TerminalBuffer(20, 5); }

        @Test
        @DisplayName("Default attributes are all-default")
        void defaultAttributes() {
            CellAttributes attrs = buf.getCurrentAttributes();
            assertNull(attrs.getForegroundColor());
            assertNull(attrs.getBackgroundColor());
            assertFalse(attrs.isBold());
            assertFalse(attrs.isItalic());
            assertFalse(attrs.isUnderline());
        }

        @Test
        @DisplayName("setForeground stores the color")
        void setForeground() {
            buf.setForeground(TerminalColor.RED);
            assertEquals(TerminalColor.RED, buf.getCurrentAttributes().getForegroundColor());
        }

        @Test
        @DisplayName("setBackground stores the color")
        void setBackground() {
            buf.setBackground(TerminalColor.BLUE);
            assertEquals(TerminalColor.BLUE, buf.getCurrentAttributes().getBackgroundColor());
        }

        @Test
        @DisplayName("null resets to default color")
        void nullResetsColor() {
            buf.setForeground(TerminalColor.RED);
            buf.setForeground(null);
            assertNull(buf.getCurrentAttributes().getForegroundColor());
        }

        @Test
        @DisplayName("Style flags are set independently")
        void styleFlags() {
            buf.setBold(true);
            buf.setItalic(true);
            buf.setUnderline(true);
            CellAttributes a = buf.getCurrentAttributes();
            assertTrue(a.isBold());
            assertTrue(a.isItalic());
            assertTrue(a.isUnderline());
        }

        @Test
        @DisplayName("resetAttributes clears everything")
        void resetAttributes() {
            buf.setForeground(TerminalColor.GREEN);
            buf.setBold(true);
            buf.resetAttributes();
            assertEquals(CellAttributes.DEFAULT, buf.getCurrentAttributes());
        }

        @Test
        @DisplayName("Attributes are applied to written cells")
        void attributesAppliedToWrittenCells() {
            buf.setForeground(TerminalColor.CYAN);
            buf.setBold(true);
            buf.writeOnLine("A");
            CellAttributes attrs = buf.getAttributesAt(0, 0);
            assertEquals(TerminalColor.CYAN, attrs.getForegroundColor());
            assertTrue(attrs.isBold());
        }

        @Test
        @DisplayName("setCurrentAttributes replaces all attributes")
        void setCurrentAttributes() {
            CellAttributes custom = new CellAttributes(
                    TerminalColor.MAGENTA, TerminalColor.WHITE, true, false, true);
            buf.setCurrentAttributes(custom);
            assertEquals(custom, buf.getCurrentAttributes());
        }

        @Test
        @DisplayName("setCurrentAttributes throws on null")
        void setCurrentAttributesNull() {
            assertThrows(NullPointerException.class, () -> buf.setCurrentAttributes(null));
        }
    }

    @Nested
    @DisplayName("Cursor")
    class CursorTests {

        TerminalBuffer buf;

        @BeforeEach
        void setUp() { buf = new TerminalBuffer(10, 5); }

        @Test
        @DisplayName("setCursorPosition sets exact position")
        void setCursorPosition() {
            buf.setCursorPosition(3, 2);
            assertEquals(3, buf.getCursorRow());
            assertEquals(2, buf.getCursorColumn());
        }

        @Test
        @DisplayName("setCursorPosition clamps to bounds")
        void setCursorPositionClamped() {
            buf.setCursorPosition(-5, -5);
            assertEquals(0, buf.getCursorColumn());
            assertEquals(0, buf.getCursorRow());

            buf.setCursorPosition(100, 100);
            assertEquals(9, buf.getCursorColumn());
            assertEquals(4, buf.getCursorRow());
        }

        @Test
        @DisplayName("moveCursorUp decrements row")
        void moveCursorUp() {
            buf.setCursorPosition(3, 3);
            buf.moveCursorUp(2);
            assertEquals(1, buf.getCursorRow());
        }

        @Test
        @DisplayName("moveCursorUp clamps at row 0")
        void moveCursorUpClamped() {
            buf.setCursorPosition(0, 1);
            buf.moveCursorUp(5);
            assertEquals(0, buf.getCursorRow());
        }

        @Test
        @DisplayName("moveCursorDown increments row")
        void moveCursorDown() {
            buf.setCursorPosition(0, 1);
            buf.moveCursorDown(2);
            assertEquals(2, buf.getCursorRow());
        }

        @Test
        @DisplayName("moveCursorDown clamps at last row")
        void moveCursorDownClamped() {
            buf.setCursorPosition(0, 3);
            buf.moveCursorDown(10);
            assertEquals(4, buf.getCursorRow());
        }

        @Test
        @DisplayName("moveCursorLeft decrements col")
        void moveCursorLeft() {
            buf.setCursorPosition(5, 5);
            buf.moveCursorLeft(3);
            assertEquals(2, buf.getCursorColumn());
        }

        @Test
        @DisplayName("moveCursorLeft clamps at col 0")
        void moveCursorLeftClamped() {
            buf.setCursorPosition(2, 5);
            buf.moveCursorLeft(10);
            assertEquals(0, buf.getCursorColumn());
        }

        @Test
        @DisplayName("moveCursorRight increments col")
        void moveCursorRight() {
            buf.setCursorPosition(3, 0);
            buf.moveCursorRight(4);
            assertEquals(4, buf.getCursorColumn());
        }

        @Test
        @DisplayName("moveCursorRight clamps at last col")
        void moveCursorRightClamped() {
            buf.setCursorPosition(8, 7);
            buf.moveCursorRight(10);
            assertEquals(9, buf.getCursorColumn());
        }

        @Test
        @DisplayName("Negative movement argument throws")
        void negativeMoveThrows() {
            assertThrows(IllegalArgumentException.class, () -> buf.moveCursorUp(-1));
            assertThrows(IllegalArgumentException.class, () -> buf.moveCursorDown(-1));
            assertThrows(IllegalArgumentException.class, () -> buf.moveCursorLeft(-1));
            assertThrows(IllegalArgumentException.class, () -> buf.moveCursorRight(-1));
        }
    }

    @Nested
    @DisplayName("writeOnLine (overwrite)")
    class WriteTextTests {

        TerminalBuffer buf;

        @BeforeEach
        void setUp() { buf = new TerminalBuffer(10, 5); }

        @Test
        @DisplayName("Writes characters at cursor and advances cursor")
        void basicWrite() {
            buf.writeOnLine("Hello");
            assertEquals("Hello     ", buf.getScreenLineString(0));
            assertEquals(5, buf.getCursorColumn());
            assertEquals(0, buf.getCursorRow());
        }

        @Test
        @DisplayName("Overwrites existing content")
        void overwritesContent() {
            buf.writeOnLine("AAAAAAAAAA");
            buf.setCursorPosition(0, 2);
            buf.writeOnLine("BB");
            assertEquals("AABBAAAAAA", buf.getLineString(0));
        }

        @Test
        @DisplayName("Characters past right edge are dropped (no wrap)")
        void noWrapAtRightEdge() {
            buf.setCursorPosition(0, 7);
            buf.writeOnLine("ABCDE"); // only ABC fit
            assertEquals("       ABC", buf.getScreenLineString(0));
            assertEquals(9, buf.getCursorColumn()); // cursor went to 10 (past last col)
        }

        @Test
        @DisplayName("Write in non-zero row")
        void writeInMiddleRow() {
            buf.setCursorPosition(1, 2);
            buf.writeOnLine("Hi");
            assertEquals("          ", buf.getScreenLineString(0));
            assertEquals("  Hi      ", buf.getScreenLineString(1));
        }

        @Test
        @DisplayName("writeText with null throws")
        void writeNull() {
            assertThrows(NullPointerException.class, () -> buf.writeOnLine(null));
        }

        @Test
        @DisplayName("Attributes are stored on each written cell")
        void attributesStoredPerCell() {
            buf.setForeground(TerminalColor.RED);
            buf.writeOnLine("AB");
            buf.setForeground(TerminalColor.GREEN);
            buf.writeOnLine("C");

            assertEquals(TerminalColor.RED, buf.getAttributesAt(0, 0).getForegroundColor());
            assertEquals(TerminalColor.RED, buf.getAttributesAt(0, 1).getForegroundColor());
            assertEquals(TerminalColor.GREEN, buf.getAttributesAt(0, 2).getForegroundColor());
        }
    }

    @Nested
    @DisplayName("insertOnLine (wrapping)")
    class InsertTextTests {

        @Test
        @DisplayName("Short text does not wrap")
        void shortTextNoWrap() {
            TerminalBuffer buf = new TerminalBuffer(10, 3);
            buf.insertOnLine("Hi");
            assertEquals("Hi        ", buf.getScreenLineString(0));
            assertEquals(2, buf.getCursorColumn());
            assertEquals(0, buf.getCursorRow());
        }

        @Test
        @DisplayName("Text wraps to next line at right edge")
        void wrapsToNextLine() {
            TerminalBuffer buf = new TerminalBuffer(5, 3);
            buf.insertOnLine("ABCDEFGH");
            assertEquals("ABCDE", buf.getScreenLineString(0));
            assertEquals("FGH  ", buf.getScreenLineString(1));
            assertEquals(3, buf.getCursorColumn());
            assertEquals(1, buf.getCursorRow());
        }

        @Test
        @DisplayName("Wrapping at bottom causes scroll")
        void wrappingAtBottomScrolls() {
            TerminalBuffer buf = new TerminalBuffer(5, 2, 100);
            buf.insertOnLine("ABCDEFGHIJ"); // exactly 2 lines
            buf.insertOnLine("K");         // forces scroll
            assertEquals(1, buf.getScrollbackSize());
            assertEquals("ABCDE", buf.getLineString(0)); // ABCDE was top row, scrolled off first
            assertEquals("K    ", buf.getScreenLineString(1));
        }

        @Test
        @DisplayName("insertOnLine with null throws")
        void insertNull() {
            TerminalBuffer buf = new TerminalBuffer(10, 3);
            assertThrows(NullPointerException.class, () -> buf.insertOnLine(null));
        }

        @Test
        @DisplayName("insertOnLine non-override mode basic")
        void insertOnLineNonOverrideModeBasic() {
            TerminalBuffer buf = new TerminalBuffer(10, 3);
            buf.insertOnLine("ABCDEFGHIJ");
            buf.setCursorPosition(0, 2);
            buf.insertOnLine("12345", false);
            assertEquals(0, buf.getCursorRow());
            assertEquals(7, buf.getCursorColumn());
            assertEquals("AB12345CDE", buf.getScreenLineString(0));
            assertEquals("FGHIJ     ", buf.getScreenLineString(1));
        }

        @Test
        @DisplayName("insertOnLine non-override mode wraps line and pushes to scrollback")
        void insertOnLineNonOverrideModeWrapsLine() {
            TerminalBuffer buf = new TerminalBuffer(10, 1);
            buf.writeOnLine("ABCDEFGHI ");
            buf.setCursorPosition(0, 2);
            buf.insertOnLine("12345", false);
            assertEquals(0, buf.getCursorRow());
            assertEquals(7, buf.getCursorColumn());
            assertEquals("AB12345CDE", buf.getScrollbackLineString(0));
            assertEquals("FGHI      ", buf.getScreenLineString(0));
        }

        @Test
        @DisplayName("insertOnLine non-override mode dispalced text ends at end of line")
        void insertOnLineNonOverrideModeDispalcedTextEndsAtEndOfLine() {
            TerminalBuffer buf = new TerminalBuffer(10, 1);
            buf.insertOnLine("ABCDE");
            buf.setCursorPosition(0, 2);
            buf.insertOnLine("12345", false);
            assertEquals(0, buf.getCursorRow());
            assertEquals(7, buf.getCursorColumn());
            assertEquals("AB12345CDE", buf.getLineString(0));
            assertEquals(1, buf.getHeight());
        }
    }

    @Nested
    @DisplayName("fillCurrentLine")
    class FillLineTests {

        TerminalBuffer buf;

        @BeforeEach
        void setUp() { buf = new TerminalBuffer(5, 3); }

        @Test
        @DisplayName("Fills entire current row with given char")
        void fillWithChar() {
            buf.setCursorPosition(1, 0);
            buf.fillCurrentLine('*');
            assertEquals("     ", buf.getScreenLineString(0));
            assertEquals("*****", buf.getScreenLineString(1));
            assertEquals("     ", buf.getScreenLineString(2));
        }

        @Test
        @DisplayName("Fill with NUL produces empty cells")
        void fillWithNul() {
            buf.writeOnLine("Hello");
            buf.setCursorPosition(0, 0);
            buf.fillCurrentLine('\0');
            assertEquals("     ", buf.getScreenLineString(0));
        }

        @Test
        @DisplayName("Fill stores current attributes on cells")
        void fillStoresAttributes() {
            buf.setBackground(TerminalColor.YELLOW);
            buf.fillCurrentLine('-');
            assertEquals(TerminalColor.YELLOW, buf.getAttributesAt(0, 0).getBackgroundColor());
            assertEquals(TerminalColor.YELLOW, buf.getAttributesAt(0, 4).getBackgroundColor());
        }


    }

    @Nested
    @DisplayName("insertEmptyLine")
    class InsertEmptyLineTests {

        @Test
        @DisplayName("Appends blank line at bottom and scrolls screen up")
        void insertsAndScrolls() {
            TerminalBuffer buf = new TerminalBuffer(5, 3, 100);
            buf.writeOnLine("AAAAA");
            buf.setCursorPosition(1, 0);
            buf.writeOnLine("BBBBB");
            buf.setCursorPosition(2, 0);
            buf.writeOnLine("CCCCC");

            buf.insertLine();

            // Screen is still 3 rows; "AAAAA" scrolled off
            assertEquals(1, buf.getScrollbackSize());
            assertEquals("AAAAA", buf.getLineString(0));
            assertEquals("BBBBB", buf.getScreenLineString(0));
            assertEquals("CCCCC", buf.getScreenLineString(1));
            assertEquals("     ", buf.getScreenLineString(2));
        }

        @Test
        @DisplayName("Screen height is maintained after insert")
        void screenHeightMaintained() {
            TerminalBuffer buf = new TerminalBuffer(5, 3, 100);
            for (int i = 0; i < 10; i++) buf.insertLine();
            assertEquals(3, buf.getHeight());
        }
    }

    @Nested
    @DisplayName("Clear operations")
    class ClearTests {

        @Test
        @DisplayName("clearScreen blanks all screen cells")
        void clearScreen() {
            TerminalBuffer buf = new TerminalBuffer(5, 3, 100);
            buf.insertOnLine("AAAAAAAAAAAAAAAA");
            buf.clearScreen();
            for (int r = 0; r < 3; r++) {
                assertEquals("     ", buf.getScreenLineString(r));
            }
        }

        @Test
        @DisplayName("clearScreen preserves scrollback")
        void clearScreenPreservesScrollback() {
            TerminalBuffer buf = new TerminalBuffer(5, 2, 100);
            buf.insertOnLine("AAAAAAAAAA"); // fills 2 lines, no scroll
            buf.insertOnLine("B");          // scrolls
            int sbBefore = buf.getScrollbackSize();
            buf.clearScreen();
            assertEquals(sbBefore, buf.getScrollbackSize());
        }

        @Test
        @DisplayName("clearScreen does not change cursor or attributes")
        void clearScreenPreservesCursorAndAttrs() {
            TerminalBuffer buf = new TerminalBuffer(10, 5);
            buf.setCursorPosition(2, 3);
            buf.setForeground(TerminalColor.RED);
            buf.clearScreen();
            assertEquals(3, buf.getCursorColumn());
            assertEquals(2, buf.getCursorRow());
            assertEquals(TerminalColor.RED, buf.getCurrentAttributes().getForegroundColor());
        }

        @Test
        @DisplayName("clearScreenAndScrollback removes scrollback too")
        void clearScreenAndScrollback() {
            TerminalBuffer buf = new TerminalBuffer(5, 2, 100);
            buf.insertOnLine("AAAAAAAAAA");
            buf.insertOnLine("B");
            assertTrue(buf.getScrollbackSize() > 0);
            buf.clearAll();
            assertEquals(0, buf.getScrollbackSize());
            for (int r = 0; r < 2; r++) {
                assertEquals("     ", buf.getScreenLineString(r));
            }
        }
    }

    @Nested
    @DisplayName("Scrollback")
    class ScrollbackTests {

        @Test
        @DisplayName("Scrollback grows as lines scroll off")
        void scrollbackGrows() {
            TerminalBuffer buf = new TerminalBuffer(5, 2, 100);
            buf.insertOnLine("AAAAAAAAAA"); // 2 lines, no scroll
            assertEquals(1, buf.getScrollbackSize());
            buf.insertOnLine("B");          // scroll 1
            assertEquals(1, buf.getScrollbackSize());
        }

        @Test
        @DisplayName("Scrollback is capped at maxScrollback")
        void scrollbackCapped() {
            TerminalBuffer buf = new TerminalBuffer(5, 2, 3);
            // Produce 10 scrollback lines
            for (int i = 0; i < 12; i++) buf.insertLine();
            assertEquals(3, buf.getScrollbackSize());
        }

        @Test
        @DisplayName("Zero maxScrollback discards all scrolled lines")
        void zeroScrollback() {
            TerminalBuffer buf = new TerminalBuffer(5, 2, 0);
            buf.insertLine();
            assertEquals(0, buf.getScrollbackSize());
        }

        @Test
        @DisplayName("Scrollback lines are accessible via absolute row index")
        void absoluteRowAccess() {
            TerminalBuffer buf = new TerminalBuffer(5, 2, 100);
            buf.writeOnLine("LINE1");
            buf.insertLine();
            buf.setCursorPosition(0,0);
            buf.insertOnLine("LINE2");
            buf.insertLine();
            // scrollback[0] = "LINE1", scrollback[1] = "LINE2", screen[0] = blank
            assertEquals("LINE1", buf.getLineString(0));
            assertEquals("LINE2", buf.getLineString(1));
            assertEquals("     ", buf.getLineString(2));
        }

        @Test
        @DisplayName("getFullContent includes scrollback then screen")
        void fullContent() {
            TerminalBuffer buf = new TerminalBuffer(3, 2, 100);
            buf.insertOnLine("ABC");

            buf.insertOnLine("DEF");
            String full = buf.toString();
            assertEquals("ABC\nDEF\n   \n", full);
        }
    }

    @Nested
    @DisplayName("Content access")
    class ContentAccessTests {

        TerminalBuffer buf;

        @BeforeEach
        void setUp() {
            buf = new TerminalBuffer(10, 5, 100);
        }

        @Test
        @DisplayName("getScreenCharAt returns correct character")
        void getScreenCharAt() {
            buf.writeOnLine("Hello");
            assertEquals('H', buf.getScreenCharAt(0, 0));
            assertEquals('e', buf.getScreenCharAt(0, 1));
            assertEquals('\0', buf.getScreenCharAt(0, 5)); // empty
        }

        @Test
        @DisplayName("getScreenAttributesAt returns correct attributes")
        void getScreenAttributesAt() {
            buf.setForeground(TerminalColor.BLUE);
            buf.writeOnLine("X");
            assertEquals(TerminalColor.BLUE, buf.getScreenAttributesAt(0, 0).getForegroundColor());
        }

        @Test
        @DisplayName("getScreenLineAsString returns the full padded line")
        void getScreenLineAsString() {
            buf.writeOnLine("Hi");
            assertEquals("Hi        ", buf.getScreenLineString(0));
        }

        @Test
        @DisplayName("getScreenContent returns all screen rows separated by newlines")
        void getScreenContent() {
            buf.writeOnLine("Row0");
            buf.setCursorPosition(1, 0);
            buf.writeOnLine("Row1");
            String content = buf.getScreenString();
            String[] lines = content.split("\n");
            assertEquals(5, lines.length);
            assertEquals("Row0      ", lines[0]);
            assertEquals("Row1      ", lines[1]);
        }

        @Test
        @DisplayName("getCharAt works for both scrollback and screen")
        void getCharAt() {
            buf.writeOnLine("ABCDEFGHIJ"); // fills row 0
            buf.insertLine();       // row 0 moves to scrollback
            buf.setCursorPosition(0,0);
            buf.writeOnLine("XYZ");

            // Scrollback row 0
            assertEquals('A', buf.getCharAt(0, 0));
            // Screen row 0 (absolute row 1)
            assertEquals('X', buf.getCharAt(1, 0));
        }

        @Test
        @DisplayName("Out-of-range access throws")
        void outOfRangeThrows() {
            assertThrows(IndexOutOfBoundsException.class, () -> buf.getScreenCharAt(0, 10));
            assertThrows(IndexOutOfBoundsException.class, () -> buf.getLineString(-1));
        }

        @Test
        @DisplayName("Get scrollback character")
        void getScrollbackChar() {
            buf.writeOnLine("ABCDEFGHIJ");
            buf.insertLine();
            buf.setCursorPosition(0,0);
            assertEquals('A', buf.getScrollbackCharAt(0,0));
            assertEquals('B', buf.getScrollbackCharAt(0,1));
            buf.writeOnLine("XYZ");
            buf.insertLine();
            assertEquals('A', buf.getScrollbackCharAt(1,0));
            assertEquals('X', buf.getScrollbackCharAt(0,0));
        }

        @Test
        @DisplayName("Get scrollback attributes")
        void getScrollbackAttributes() {
            buf.writeOnLine("ABCDEFGHIJ");
            buf.insertLine();
            buf.setCursorPosition(0,0);
            CellAttributes attr = new CellAttributes(TerminalColor.RED, TerminalColor.BLUE, true, false, true);
            assertNotEquals(attr, buf.getScrollbackAttributesAt(0,0));
            buf.setCurrentAttributes(attr);
            buf.writeOnLine("XYZ");
            assertEquals(attr, buf.getScreenAttributesAt(0,0));
            buf.insertLine();
            assertEquals('X', buf.getScrollbackCharAt(0,0));
            assertEquals(attr, buf.getScrollbackAttributesAt(0,0));
        }

        @Test
        @DisplayName("Get scrollback line as string")
        void getScrollbackLineAsString() {
            buf.writeOnLine("ABCDEFGHIJ");
            buf.insertLine();
            buf.setCursorPosition(0,0);
            buf.writeOnLine("ZZZZZZZ");
            buf.insertLine();
            buf.insertLine();
            assertEquals("ABCDEFGHIJ", buf.getScrollbackLineString(2));
            assertEquals("ZZZZZZZ   ", buf.getScrollbackLineString(1));
            assertEquals("          ", buf.getScrollbackLineString(0));
        }
    }

    @Nested
    @DisplayName("Wide characters")
    class WideCharacters {

        @Test
        @DisplayName("WideCharUtil detects CJK characters")
        void detectsCJK() {
            assertTrue(WideCharUtil.isWide('\u4E2D')); // 中
            assertTrue(WideCharUtil.isWide('\u6587')); // 文
            assertFalse(WideCharUtil.isWide('A'));
            assertFalse(WideCharUtil.isWide('1'));
        }

        @Test
        @DisplayName("Wide char advances cursor by 2")
        void wideCursorAdvance() {
            TerminalBuffer buf = new TerminalBuffer(10, 3);
            buf.writeOnLine("\u4E2D"); // 中
            assertEquals(2, buf.getCursorColumn());
        }

        @Test
        @DisplayName("Wide char sets continuation cell")
        void wideContinuationCell() {
            TerminalBuffer buf = new TerminalBuffer(10, 3);
            buf.writeOnLine("\u4E2D");
            assertEquals('\u4E2D', buf.getScreenCharAt(0, 0));
            assertEquals('\0',    buf.getScreenCharAt(0, 1)); // continuation
        }

        @Test
        @DisplayName("Mixed wide and narrow characters in writeText")
        void mixedWidthWrite() {
            TerminalBuffer buf = new TerminalBuffer(10, 3);
            buf.writeOnLine("A\u4E2DB"); // A + 中 (wide) + B
            // A at col 0, 中 at col 1+2, B at col 3
            assertEquals('A',     buf.getScreenCharAt(0, 0));
            assertEquals('\u4E2D', buf.getScreenCharAt(0, 1));
            assertEquals('\0',    buf.getScreenCharAt(0, 2)); // continuation
            assertEquals('B',     buf.getScreenCharAt(0, 3));
            assertEquals(4, buf.getCursorColumn());
        }

        @Test
        @DisplayName("Wide char at end of line is dropped in writeOnLine (no room for continuation)")
        void wideCharAtEndDropped() {
            // Buffer width 3, write two narrow + one wide: wide is at col 2 but needs col 3
            TerminalBuffer buf = new TerminalBuffer(3, 3);
            buf.writeOnLine("AB\u4E2D"); // AB = 2 cols, 中 needs 2 cols but only 1 left, so it is dropped
            assertEquals('A', buf.getScreenCharAt(0, 0));
            assertEquals('B', buf.getScreenCharAt(0, 1));
            assertEquals('\0', buf.getScreenCharAt(0, 2));
        }

        @Test
        @DisplayName("insertText wraps before wide char that won't fit in last column")
        void insertTextWrapsBeforeWide() {
            TerminalBuffer buf = new TerminalBuffer(5, 3);
            // Fill 4 columns then try to insert a wide char: should wrap to next line
            buf.writeOnLine("ABCD"); // cursor now at col 4 (last col)
            buf.insertOnLine("\u4E2D"); // wide char, needs 2 cols, only 1 left → wrap
            assertEquals('\0', buf.getScreenCharAt(0, 4)); // last col of row 0 untouched
            assertEquals('\u4E2D', buf.getScreenCharAt(1, 0)); // wide char on row 1
            assertEquals('\0',    buf.getScreenCharAt(1, 1)); // continuation
        }
    }

}