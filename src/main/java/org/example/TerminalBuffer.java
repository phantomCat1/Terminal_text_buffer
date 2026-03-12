package org.example;

import java.util.*;

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
            screen.add(new Line(width, new Cell(Cell.EMPTY_CHARACTER, attributes, false, false)));
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
    public List<Line> getScreen() {return screen;}
    public List<Line> getScrollBack() {return scrollBack.stream().toList();}

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
            throw new NullPointerException("Attributes cannot be null");
        }
        this.attributes = attrs;
    }

    /** Sets the foreground color ({@code null} = default terminal foreground). */
    public void setForeground(TerminalColor fg) {
        if (fg == null) {
            this.attributes = this.attributes.setDefaultForegroundColor();
            return;
        }
        this.attributes = this.attributes.setForegroundColor(fg);
    }

    /** Sets the background color ({@code null} = default terminal background). */
    public void setBackground(TerminalColor bg) {
        if (bg == null) {
            this.attributes = this.attributes.setDefaultBackgroundColor();
            return;
        }
        this.attributes = this.attributes.setBackgroundColor(bg);
    }

    /** Enables or disables the bold style flag. */
    public void setBold(boolean bold) {
        this.attributes = this.attributes.setBold(bold);
    }

    /** Enables or disables the italic style flag. */
    public void setItalic(boolean italic) {
        this.attributes = this.attributes.setItalic(italic);
    }

    /** Enables or disables the underline style flag. */
    public void setUnderline(boolean underline) {
        this.attributes = this.attributes.setUnderline(underline);
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
        this.cursorColumn  = Math.min(width-1, this.cursorColumn + N);
    }

    //_______________________________________________________________
    //Editing methods
    //_______________________________________________________________
    /** Writes text on the line beginning at the cursor position on the screen, overriding the existing text.
     * Uses the terminal's attributes
     *  Does not perform wrapping and characters are just dropped if the width is exceeded
     *  Moves the cursor to the end of the text or if text reaches the width of the terminal, it remains at the last cell.
     *  Needs to manually have the cursor position changed to write anything else in the last case, using {@code setCursorPosition(row, col)}
     * Wide characters advance the cursor by 2. If a wide character starts/ is written to column width-1, then it is dropped, instead of being written only half.
     * @param text text to be written on this line
     * */
    public void writeOnLine(String text) {
        if(text == null) throw new NullPointerException("Text cannot be null");
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if(WideCharUtil.isWide(ch) && this.cursorColumn + 1 >= width) return;
            if(this.cursorColumn >= width) {
                this.cursorColumn = width-1;
                return;
            }
            writeCharToCursor(ch);
        }
    }

    /**
     * Inserts text on the current line of the cursor, beginning at cursor position, using the terminal attributes.
     * Has two modes of operation: override and non-override
     * In override mode it overrides the text already present.
     * In non-override mode it pushes the existing text forward.
     * Wraps around the line if text exceeds width of screen. It creates a new line and pushes the oldest one to
     * the scrollback if text is inserted on the last line of the screen.
     * A wide character that starts on the width-1 column is fully wrapped to the next line (no splitting).
     * Moves cursor to the end of text. Wide characters advance the cursor by 2.
     * @param text to be inserted on this line
     */
    public void insertOnLine(String text) {
        insertOnLine(text, true);
    }
    public void insertOnLine(String text, boolean override) {
        if(text == null) throw new NullPointerException("Text cannot be null");
        String toInsert = text;
        int newCursorColumn = (this.cursorColumn + text.length()) % width;
        int newCursorRow = this.cursorRow+ (this.cursorColumn + text.length()) / width;
        if(!override) {
            StringBuilder sb = new StringBuilder(text.length() + 1);
            sb.append(text);
            int col = this.cursorColumn;
            int row = this.cursorRow;
            char ch = getScreenCharAt(row, col);
            while(ch != '\0') {
                sb.append(ch);
                col += 1;
                if(col >= width) {
                    col = 0;
                    row += 1;
                    if(row >= height) break;
                }
                ch = getScreenCharAt(row, col);
            }
            toInsert = sb.toString();
        }
        for (int i = 0; i < toInsert.length(); i++) {
            char ch = toInsert.charAt(i);
            boolean wide = WideCharUtil.isWide(ch);
            if(wide && this.cursorColumn == width-1) advanceCursorToNextLine();
            if(this.cursorColumn >= width) advanceCursorToNextLine();
            writeCharToCursor(ch);
        }
        if(this.cursorColumn >= width && override) advanceCursorToNextLine();
        else setCursorPosition(newCursorRow, newCursorColumn);
    }

    /**
     * Fills all cells in a line with the given character, keeping the current set attributes.
     * Cannot fill the line with wide characters.
     * Does not move the cursor.
     * @param lineNumber line to be filled
     * @param character character to fill the line with
     */
    public void fillLine(int lineNumber, char character) {
        if(lineNumber < 0) throw new IllegalArgumentException("lineNumber must be >= 0");
        if(lineNumber >= height) throw new IllegalArgumentException("lineNumber must be <= width");
        if (character != Cell.EMPTY_CHARACTER && WideCharUtil.isWide(character)) {
            throw new IllegalArgumentException("Cannot fill line with a wide character");
        }
        Cell fillCell = (character == Cell.EMPTY_CHARACTER) ? Cell.EMPTY : new Cell(character, attributes, false, false);
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
        screen.add(new Line(width, new Cell(Cell.EMPTY_CHARACTER, attributes, false, false)));

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
    //Resize buffer methods
    //_______________________________________________________________
    /**
     * This method handles resize of the terminal buffer. It allows a minimum new height and width of 1.
     * It resizes both the screen and the scrollback to ensure consistent behavior.
     * Width resize was implemented by Claude, with me checking the output and guiding it further until the desired behavior was achieved.
     * I also made sure that object copies or use of references is correct and somewhat optimal.
     * If width increases, rows are padded with cells matching the current attributes and text that was previously wrapped gets unwrapped.
     *      This implemented by setting a {@code wasWrapped} flag to true whenever an {@code insertOnLine()} operation or a width decrease is used.
     * If width decreases, empty cells are truncated while text gets wrapped, potentially pushing top lines to the scrollback.
     * If height increases, empty rows (with the current attributes) are simply inserted in the screen.
     * If height decreases, empty rows are truncated while the bottom rows of text are maintained, potentially pushing the top rows to scrollback.
     *      This is achieved by having each row maintain a {@code isDirty} boolean that keeps track if the line contains any non-empty cells.
     */
    public void resize(int newWidth, int newHeight){
        newHeight = Math.max(1, newHeight);
        newWidth = Math.max(1, newWidth);

        if(newWidth > width) increaseWidthBy(newWidth - width);
        else if(newWidth < width) decreaseWidthBy(width - newWidth);

        width = newWidth;

        if(newHeight > height) increaseHeightBy(newHeight - height);
        else if(newHeight < height) decreaseHeightBy(height - newHeight);

        height = newHeight;

        cursorColumn = clamp(cursorColumn, 0, width  - 1);
        cursorRow = clamp(cursorRow, 0, height - 1);

    }

    private void increaseWidthBy(int increaseBy){
        int newWidth = width + increaseBy;

        // Build a unified list (scrollback first, then screen) so the unwrap
        // pass can see across the scrollback/screen boundary uniformly.
        List<Line> all = new ArrayList<>(scrollBack.size() + screen.size());
        // copy to avoid aliasing
        all.addAll(scrollBack);
        all.addAll(screen);

        // Reflow pass: walk each line.  If isSoftWrapped() is true, this line
        // and its successor belong to the same logical paragraph — append
        // content without a hard break so they can be re-partitioned together.
        // Otherwise insert a hard break (pad to the next line boundary).
        List<Cell> cellStream = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            Line cur = all.get(i);
            appendNonTrailingEmpty(cellStream, cur);
            if (!cur.wasWrapped()) {
                // Hard line break: pad to the next newWidth boundary.
                padCellStreamToLineEnd(cellStream, newWidth);
            }
            // If softWrapped, we simply continue — no padding — so the content
            // of the next line is appended directly after this one.
        }
        // Ensure the stream length is a multiple of newWidth.
        padCellStreamToLineEnd(cellStream, newWidth);

        // Partition the merged stream back into Lines of newWidth.
        // Continuation lines produced here are NOT soft-wrapped (the logical
        // paragraph still fits within newWidth or will be split again naturally).
        List<Line> reflowed = partitionIntoLines(cellStream, newWidth);

        // Write back into scrollback and screen, preserving the original
        // scrollback size.
        int sbSize = scrollBack.size();
        scrollBack.clear();
        for (int j = 0; j < sbSize && j < reflowed.size(); j++) {
            scrollBack.addLast(reflowed.get(j));
        }
        while (scrollBack.size() > maxScrollBack) scrollBack.removeFirst();

        screen.clear();
        for (int j = sbSize; j < reflowed.size(); j++) {
            screen.add(reflowed.get(j));
        }
        // Re-establish screen height.
        while (screen.size() < height) screen.add(new Line(newWidth));
        while (screen.size() > height) pushTopLineToScrollback();
    }

    private void decreaseWidthBy(int decreaseBy){
        int newWidth = width - decreaseBy;

        List<Line> reflowed = new ArrayList<>();

        List<Line> allLines = new ArrayList<>(scrollBack.size() + screen.size());
        allLines.addAll(scrollBack);
        allLines.addAll(screen);

        for (Line line : allLines) {

            if (line.isNotDirty()) {
                // Blank line — keep as a single blank line at the new width.

                reflowed.add(new Line(newWidth));
            } else {
                // Collect the content cells (strip trailing empty).

                List<Cell> content = new ArrayList<>();
                appendNonTrailingEmpty(content, line);
                padCellStreamToLineEnd(content, newWidth);

                List<Line> wrapped = partitionIntoLines(content, newWidth);

                // Every line except the last is a soft-wrap continuation —
                // they were created by this resize and must be undoable.
                for (int i = 0; i < wrapped.size() - 1; i++) {
                    wrapped.get(i).setWrapped(true);
                }
                // The final line inherits the original line's soft-wrap flag:
                // if the original was itself soft-wrapped (i.e. it was a
                // continuation from a previous wrap), the last chunk continues
                // that paragraph; otherwise it is a hard line end.
                wrapped.getLast().setWrapped(line.wasWrapped());

                reflowed.addAll(wrapped);
            }
        }

        // Write scrollback portion back.
        int sbSize = scrollBack.size();
        scrollBack.clear();
        for (int j = 0; j < sbSize && j < reflowed.size(); j++) {
            scrollBack.addLast(reflowed.get(j));
        }
        while (scrollBack.size() > maxScrollBack) scrollBack.removeFirst();

        // Write screen portion back.
        screen.clear();
        for (int j = sbSize; j < reflowed.size(); j++) {
            screen.add(reflowed.get(j));
        }
        while (screen.size() > height) pushTopLineToScrollback();
        while (screen.size() < height) screen.add(new Line(newWidth));
    }

    private void increaseHeightBy(int increaseBy){
        for(int i=0; i< increaseBy; i++){
            screen.add(new Line(width, Cell.EMPTY));
        }
    }

    private void decreaseHeightBy(int decreaseBy){
        int toRemove = decreaseBy;

        // first drop non-dirty/ empty rows from the bottom.
        while (toRemove > 0 && !screen.isEmpty() && screen.getLast().isNotDirty()) {
            screen.removeLast();
            toRemove--;
        }

        // push top rows (dirty or not) to scrollback.
        while (toRemove > 0 && !screen.isEmpty()) {
            pushTopLineToScrollback();
            toRemove--;
        }
    }

    //----------------------------------------------------------------
    // Resize utilities
    //----------------------------------------------------------------
    /**
     * Appends all cells of {@code line} except trailing empty cells to {@code out}.
     * Used so that when two soft-wrapped lines are merged there is no gap of
     * empty cells between their content.
     */
    private static void appendNonTrailingEmpty(List<Cell> out, Line line) {

        // Find rightmost non-empty cell
        int last = -1;
        for (int i = line.getWidth()-1; i >= 0; i--) {
            Cell c = line.getCell(i);
            if (!c.isEmpty() && !c.isWideContinuation()) { last = i; break; }
        }
        for (int i = 0; i <= last; i++) {
            out.add(line.getCell(i));
        }
    }

    /**
     * Pads {@code stream} with empty cells up to the next multiple of
     * {@code lineWidth}, creating a hard line break in the reflow output.
     */
    private void padCellStreamToLineEnd(List<Cell> stream, int lineWidth) {
        int rem = stream.size() % lineWidth;
        if (rem != 0) {
            int pad = lineWidth - rem;
            for (int i = 0; i < pad; i++) stream.add(Cell.EMPTY);
        }
    }

    /**
     * Partitions a flat list of cells into {@link Line}s of {@code lineWidth} each.
     * Wide cells that would straddle a line boundary are pushed to the next line.
     * The returned lines have {@code softWrapped = false}; callers are responsible
     * for stamping the flag where appropriate.
     */
    private List<Line> partitionIntoLines(List<Cell> cells, int lineWidth) {
        List<Line> result = new ArrayList<>();
        int i = 0;
        while (i < cells.size()) {
            Line line = new Line(lineWidth);
            int col = 0;
            while (col < lineWidth && i < cells.size()) {
                Cell cell = cells.get(i);
                if (cell.isWide()) {
                    if (col + 1 >= lineWidth) {
                        // Wide char won't fit — leave this column empty and wrap.
                        break;
                    }
                    line.setCell(col, cell); // setCell handles the continuation
                    col += 2;
                } else {
                    line.setCell(col, cell);
                    col++;
                }
                i++;
            }
            // Skip any wide-continuation tokens left in the stream (they were
            // regenerated by setCell above and should not be placed again).
            while (i < cells.size() && cells.get(i).isWideContinuation()) i++;
            result.add(line);
        }
        return result;
    }

    //_______________________________________________________________
    //Content access methods
    //_______________________________________________________________

    /** Returns the character at the given absolute position across the
     * combined scrollback + screen buffer.
     * @param row 0 is the oldest scrollback line
     * @return either a character or {@code '\0'} if empty */
    public char getCharAt(int row, int column) {
        return getActualLine(row).getCell(column).getCharacter();
    }

    /**
     * Returns character at a particular position on screen.
     * @return character at position if input is correct
     */
    public char getScreenCharAt(int row, int col) {
        return screen.get(checkScreenRow(row)).getCell(col).getCharacter();
    }
    /**
     * Returns character at a particular position on scrollback.
     * @return character at position if input is correct
     */
    public char getScrollbackCharAt(int row, int col) {
        if(row < 0 || row >= scrollBack.size()) throw new IllegalArgumentException("row out of bounds");
        int idx = 0;
        Iterator<Line> it = scrollBack.descendingIterator();
        while (it.hasNext()) {
            Line line = it.next();
            if (idx == row) {
                return line.getCell(col).getCharacter();
            };
            idx++;
        }
        return '\0';
    }

    /**  Returns the {@link CellAttributes} at the given absolute position. */
    public CellAttributes getAttributesAt(int row, int column) {
        return getActualLine(row).getCell(column).getAttributes();
    }

    /** Return the attributes of a cell on the screen at the given position*/
    public CellAttributes getScreenAttributesAt(int row, int col) {
        return screen.get(checkScreenRow(row)).getCell(col).getAttributes();
    }
    /** Return the attributes of a cell on the screen at the given position*/
    public CellAttributes getScrollbackAttributesAt(int row, int col) {
        if(row < 0 || row >= scrollBack.size()) throw new IllegalArgumentException("row out of bounds");
        int idx = 0;
        Iterator<Line> it = scrollBack.descendingIterator();
        while (it.hasNext()) {
            Cell cell =  it.next().getCell(col);
            if (idx == row) {
                return cell.getAttributes();
            };
            idx++;
        }
        return CellAttributes.DEFAULT;
    }

    /** Converts a {@code Line} at the absolute {@code row} (any line from the combined screen and scrollback) to string*/
    public String getLineString(int row){
        return getActualLine(row).toPlainString();
    }
    public String getScreenLineString(int row){
        if(row < 0) throw new IllegalArgumentException("row must be >= 0");
        if(row >= height) throw new IllegalArgumentException("row must be <= height");
        return screen.get(row).toPlainString();
    }

    /**
     * Returns the Line at the given {@code row} beginning from the last added one as a string.
     * @param row 0 is the last line that was added to scrollback (most recent line)
     * @return
     */
    public String getScrollbackLineString(int row){
        if(row < 0) throw new IllegalArgumentException("row must be >= 0");
        if(row >= scrollBack.size()) throw new IllegalArgumentException("row must be <= size of scrollback");
        int idx = 0;
        Iterator<Line> it = scrollBack.descendingIterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            Line line = it.next();
            if (idx == row) {
                sb.append(line.toPlainString());
                break;
            };
            idx++;
        }
        return sb.toString();
    }

    /** Method to convert the {@code screen} to string*/
    public String getScreenString() {
        StringBuilder sb = new StringBuilder(height * (width + 1));
        for(Line line : screen){
            sb.append(line.toPlainString()).append('\n');
        }
        return sb.toString();
    }
    /** Method to convert the {@code scrollback} to string*/
    public String getScrollbackString(){
        StringBuilder sb = new StringBuilder(scrollBack.size() * (width + 1));
        for(Line line : scrollBack) {
            sb.append(line.toPlainString()).append('\n');
        }
        return sb.toString();
    }
    /** Used to get the combined scrollback and screen as string*/
     @Override
     public String toString() {
         int totalLines = scrollBack.size() + height;
        StringBuilder sb = new StringBuilder(totalLines * (width + 1));
        String screenString = getScreenString();
        String scroolbackString = getScrollbackString();
        sb.append(scroolbackString).append(screenString);
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
        boolean wide = WideCharUtil.isWide(ch);
        Line toWrite = screen.get(this.cursorRow);
        Cell cell = toWrite.getCell(this.cursorColumn);
        cell.setCharacter(ch);
        if (wide) cell.setWide(true);
        this.cursorColumn += wide ? 2 : 1;
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
            screen.add(new Line(width, new Cell(Cell.EMPTY_CHARACTER, attributes, false, false)));
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

    private int checkScreenRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException(
                    "Screen row " + row + " out of range [0, " + height + ")");
        }
        return row;
    }


}
