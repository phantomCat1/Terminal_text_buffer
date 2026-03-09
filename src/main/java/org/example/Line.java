package org.example;

import java.util.ArrayList;
import java.util.List;

public class Line {
    // Line could be based on an array or an ArrayList. If the user wouldn't resize the text buffer too often, then an array is perfectly fine.
    // If the text buffer gets resized consistently, as simple array would require frequent deep copies, which is quite expensive.
    // The Arraylist case can cover both cases if you initialize it with a given size, so you minimize the number of useless internal re-allocations.
    // Then, it makes the second scenario less expensive.
    private List<Cell> cells;

    public Line(int width) {
        this.cells = new ArrayList<>(width);
    }

    public Line(List<Cell> cells) {
        this.cells = new ArrayList<>(cells);
    }

    public int getWidth() {
        return this.cells.size();
    }

    public Cell getCell(int index){
        checkColumn(index);
        return this.cells.get(index);
    }

    public void setCell(int index, Cell cell){
        checkColumn(index);
        this.cells.set(index, cell);
    }

    // Fill all cells in the list with a particular one
    public void fill(Cell cell) {
        cells.replaceAll(ignored -> cell);
    }

    private void checkColumn(int col) {
        if (col < 0 || col >= this.cells.size()) {
            throw new IndexOutOfBoundsException("Column out of bounds");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Cell cell : cells) {
            sb.append(cell.isEmpty() ? ' ': cell.getCharacter());
        }
        return "Line [" + sb.toString() + "]";
    }
}
