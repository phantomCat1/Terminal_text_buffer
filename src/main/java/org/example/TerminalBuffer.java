package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TerminalBuffer {
    private int width;
    private int height;
    private int maxScrollBack;
    private int cursorRow;
    private int cursorColumn;
    // All cells are supposed to have the same attributes
    private CellAttributes attributes;

    // The first element represents the top line, the last element is the last line, the one at height -1 (assuming we measure height and width from the top left corner)
    private final List<Line> screen;

    // First element is the oldest line that will be eliminated next, last element is the most recent line added to scrollback.
    private final Deque<Line> scrollBack;

    public TerminalBuffer(int width, int height, int maxScrollBack, CellAttributes attributes) {

        if (width <= 0) throw new IllegalArgumentException("Width must be >0, got: " + width);
        if (height <= 0) throw new IllegalArgumentException("Height must be >0, got: " + height);
        if (maxScrollBack < 0) throw new IllegalArgumentException("MaxScrollBack must be >=0, got: " + width);
        this.width = width;
        this.height = height;
        this.maxScrollBack = maxScrollBack;
        this.screen = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            screen.add(new Line(width, new Cell('\0', attributes)));
        }
        this.scrollBack =  new ArrayDeque<>();
        this.cursorRow = 0;
        this.cursorColumn = 0;
        this.attributes = attributes;
    }
    public TerminalBuffer(int width, int height, int maxScrollBack) {
        this(width, height, maxScrollBack,  CellAttributes.DEFAULT);
    }
    public TerminalBuffer(int width, int height) {
         this(width, height, 100,  CellAttributes.DEFAULT);
    }

    //_______________________________________________________________
    //Access methods
    //_______________________________________________________________
    public int getWidth()          { return width; }
    public int getHeight()         { return height; }
    public int getMaxScrollback()  { return maxScrollBack; }
    public int getScrollbackSize() { return scrollBack.size(); }

    //_______________________________________________________________
    //Attributes methods
    //_______________________________________________________________

    public CellAttributes getCurrentAttributes() {
        return this.attributes;
    }

    /**
     * Replaces the current attributes. All cells in the screen, where the user can write, should have these attributes.
     *
     * @param attrs non-null attributes
     */
    public void setCurrentAttributes(CellAttributes attrs) {
        if (attrs == null) {
            throw new IllegalArgumentException("Attributes cannot be null");
        }
        this.attributes = attrs;
    }

    /** Sets the foreground color ({@code null} = default terminal foreground). */
    public void setForeground(TerminalColor fg) {
        if (fg == null) {
            this.attributes.setDefaultForegroundColor();
            return;
        }
        this.attributes.setForegroundColor(fg);
    }

    /** Sets the background color ({@code null} = default terminal background). */
    public void setBackground(TerminalColor bg) {
        if (bg == null) {
            this.attributes.setDefaultBackgroundColor();
            return;
        }
        this.attributes.setBackgroundColor(bg);
    }

    /** Enables or disables the bold style flag. */
    public void setBold(boolean bold) {
        this.attributes.setBold(bold);
    }

    /** Enables or disables the italic style flag. */
    public void setItalic(boolean italic) {
        this.attributes.setItalic(italic);
    }

    /** Enables or disables the underline style flag. */
    public void setUnderline(boolean underline) {
        this.attributes.setUnderline(underline);
    }

    /** Resets all current attributes to their defaults. */
    public void resetAttributes() {
        this.attributes = CellAttributes.DEFAULT;
    }

    //_______________________________________________________________
    //Cursor methods
    //_______________________________________________________________
    public int getCursorRow() {
        return cursorRow;
    }
    public int getCursorColumn() {
        return cursorColumn;
    }
    /** Sets cursor to position {@code cursorRow cursorColumn}
     * If the cursor position is set to a coordinate outside the given width* height area, then default to the edge of the boundary. */
    public void setCursorPosition(int cursorRow, int cursorColumn) {
        this.cursorColumn = clamp(cursorColumn, 0, width-1);
        this.cursorRow = clamp(cursorRow, 0, height-1);
    }

    /** Methods to move the cursor {@code N} cells up, down, left, right, respectively.
     * If index exceeds screen size, clamp to border.
     * @param N must be non-negative
     * */
    public void moveCursorUp(int N){
        if (N < 0) throw new IllegalArgumentException("N must be >= 0");
        this.cursorRow  = Math.max(0, this.cursorRow - N);
    }
    public void moveCursorDown(int N){
        if (N < 0) throw new IllegalArgumentException("N must be >= 0");
        this.cursorRow  = Math.min(height-1, this.cursorRow + N);
    }
    public void moveCursorLeft(int N){
        if (N < 0) throw new IllegalArgumentException("N must be >= 0");
        this.cursorColumn = Math.max(0, this.cursorColumn - N);
    }
    public void moveCursorRight(int N){
        if (N < 0) throw new IllegalArgumentException("N must be >= 0");
        this.cursorRow  = Math.min(width-1, this.cursorRow + N);
    }

    //_______________________________________________________________
    //Editing methods
    //_______________________________________________________________
    /** Writes text on the line beginning at the cursor position on the screen, overriding the existing text
     *  Does not perform wrapping and characters are just dropped if the width is exceeded
     *  Moves the cursor to the end of the text
     * @param text text to be written on this line
     * */
    public void writeOnLine(String text) {
        if(text == null) throw new IllegalArgumentException("Text cannot be null");
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if(this.cursorColumn >= width) return;
            writeCharToCursor(ch);
        }
    }

    /**
     * Inserts text on the current line of the cursor, beginning at cursor position.
     * Wraps around the line if text exceeds width of screen, possibly overriding the text already added
     * Moves cursor to the end of text
     * @param text to be inserted on this line
     */
    public void insertOnLine(String text) {
        if(text == null) throw new IllegalArgumentException("Text cannot be null");
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if(this.cursorColumn >= width) advanceCursorToNextLine();
            writeCharToCursor(ch);
        }
    }

    /**
     * Fills all cells in a line with the given character, keeping the current set attributes
     * Does not move the cursor
     * @param lineNumber line to be filled
     * @param character character to fill the line with
     */
    public void fillLine(int lineNumber, char character) {
        if(lineNumber < 0) throw new IllegalArgumentException("lineNumber must be >= 0");
        if(lineNumber >= height) throw new IllegalArgumentException("lineNumber must be <= width");
        Cell fillCell = character == '\0' ? Cell.EMPTY : new Cell(character, attributes);
        screen.get(lineNumber).fill(fillCell);
    }

    /**
     * Fills the line at the current position of the cursor with a given character or EMPTY
     * Does not move the cursor
     * @param character character to fill the line with
     */
    public void fillCurrentLine(char character) {
        fillLine(cursorRow, character);
    }

    /**
     * Inserts one empty line at the bottom of the screen.
     * Pushes the top line of the screen into the scrollback and maintains scrollback buffer to a
     * maximum size of maxScrollBack
     * Does not move the cursor, does not depend on attributes
     */
    public void insertLine(){
        pushTopLineToScrollback();
        screen.add(new Line(width, Cell.EMPTY));

    }

    /**
     * Clears entire screen and adds EMPTY cells
     * Does not depend on or move cursor.
     * Does not depend on attributes
     */
    public void clearScreen(){
        for(var line : screen){
            line.fill(Cell.EMPTY);
        }
    }

    /**
     * Clears the screen and empties the scrollback buffer.
     * Does not depend on or move cursor.
     * Does not depend on attributes
     */
    public void clearAll(){
        clearScreen();
        scrollBack.clear();
    }

    //_______________________________________________________________
    //Content access methods
    //_______________________________________________________________

    /** Returns the character at the given <em>absolute</em> position across the
     * combined scrollback + screen buffer.
     * @param row 0 is the oldest scrollback line
     * @return either a character or {@code '\0'} if empty */
    public char getCharAt(int row, int column) {
        return getActualLine(row).getCell(column).getCharacter();
    }

    /**  Returns the {@link CellAttributes} at the given absolute position. */
    public CellAttributes getAttributesAt(int row, int column) {
        return getActualLine(row).getCell(column).getAttributes();
    }

    /** Converts a {@code Line} at the absolute {@code row} (any line from the combined screen and scrollback) to string*/
    public String getLineString(int row){
        return getActualLine(row).toString();
    }

    /** Method to convert the {@code screen} to string*/
    public String getScreenString() {
        StringBuilder sb = new StringBuilder();
        for(Line line : screen) {
            sb.append(line.toString()).append('\n');
        }
        return sb.toString();
    }
    /** Method to convert the {@code scrollback} to string*/
    public String getScroolbackString(){
        StringBuilder sb = new StringBuilder();
        for(Line line : scrollBack) {
            sb.append(line.toString()).append('\n');
        }
        return sb.toString();
    }
    /** Used to get the combined scrollback and screen as string*/
     @Override
     public String toString() {
        StringBuilder sb = new StringBuilder();
        String screenString = getScreenString();
        String scroolbackString = getScroolbackString();
        sb.append(screenString).append('\n').append(scroolbackString);
        return sb.toString();
     }

     //------------------------------------------------------
    // Private mthods
    //-------------------------------------------------------
    /**
     * Returns the {@link Line} at an absolute row index (scrollback + screen).
     *
     * @param row 0 = oldest scrollback line
     */
    private Line getActualLine(int row) {
        int sbSize = scrollBack.size();
        int total = sbSize + height;
        if (row < 0 || row >= total) {
            throw new IndexOutOfBoundsException("Absolute row " + row + " out of range [0, " + total + ")");
        }
        if (row < sbSize) {
            int idx = 0;
            for (Line line : scrollBack) {
                if (idx == row) return line;
                idx++;
            }
        }
        return screen.get(row - sbSize);
    }

    /** Utility method to help restrict cursor position to width and height */
    private int clamp(int pos, int min, int max){
        return Math.max(min, Math.min(max, pos));
    }

    private void writeCharToCursor(char ch) {
        if (this.cursorColumn >= width) return;
        Line toWrite = screen.get(this.cursorRow);
        Cell cell = toWrite.getCell(this.cursorColumn);
        cell.setCharacter(ch);
        if(this.attributes.equals(cell.getAttributes())) return;
        cell.setAttributes(this.attributes);
    }


    /**
     * Moves the cursor to column 0 of the next screen row.  If already on the
     * last row, a new blank line is inserted (scrolling the screen up).
     */
    private void advanceCursorToNextLine() {
        cursorColumn = 0;
        if (cursorRow < height - 1) {
            cursorRow++;
        } else {
            // Bottom of screen: scroll up
            pushTopLineToScrollback();
            screen.add(new Line(width, new Cell('\0', attributes)));
            // cursorRow stays at height-1
        }
    }

    /**
     * Removes the top screen line and appends it to the scrollback buffer,
     * evicting the oldest scrollback entry if the limit is exceeded.
     */
    private void pushTopLineToScrollback() {
        Line top = screen.removeFirst();
        if (maxScrollBack > 0) {
            scrollBack.addLast(top);
            while (scrollBack.size() > maxScrollBack) {
                scrollBack.removeFirst();
            }
        }
    }


}
