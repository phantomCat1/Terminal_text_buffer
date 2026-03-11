package org.example;

import java.util.ArrayList;
import java.util.List;

public class Line {
    // Line could be based on an array or an ArrayList. If the user wouldn't resize the text buffer too often, then an array is perfectly fine.
    // If the text buffer gets resized consistently, as simple array would require frequent deep copies, which is quite expensive.
    // The Arraylist case can cover both cases if you initialize it with a given size, so you minimize the number of useless internal re-allocations.
    // Then, it makes the second scenario less expensive.
    private List<Cell> cells;

    public Line(int width, Cell cell) {
        if (width <= 0) throw new IllegalArgumentException("Width must be positive, got: " + width);
        if (cell == null) throw new IllegalArgumentException("Cell is null");
        this.cells = new ArrayList<>(width);
        for(int i=0; i<width; i++) {
            cells.add(new Cell(cell.getCharacter(), cell.getAttributes(), false, false));
        }
    }

    public Line(int width) {
        this(width, Cell.EMPTY);
    }

    public Line(List<Cell> cellList) {
        this.cells = new ArrayList<>();
        for(Cell c : cellList) {
            this.cells.add(new Cell(c.getCharacter(), c.getAttributes(), false, false));
        }
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
        clearWideNeighbour(index);
        Cell oldCell = this.cells.get(index);
        oldCell.setCharacter(cell.getCharacter());
        oldCell.setAttributes(cell.getAttributes());
        oldCell.setWide(cell.isWide());
        oldCell.setWideContinuation(cell.isWideContinuation());

        if(cell.isWide()) {
            if(index +1 < cells.size()){
                clearWideNeighbour(index+1);
                cells.get(index+1).setWideContinuation(true);
            }
        }
    }

    // Fill all cells in the list with a particular one
    public void fill(Cell cell) {
        if (cell.isWide()) {
            throw new IllegalArgumentException("Cannot fill a line with a wide cell");
        }
        for(Cell c : cells) {
            c.setCharacter(cell.getCharacter());
            c.setAttributes(cell.getAttributes());
        }
    }

    private void checkColumn(int col) {
        if (col < 0 || col >= this.cells.size()) {
            throw new IndexOutOfBoundsException("Column out of bounds");
        }
    }

    public String toPlainString() {
        StringBuilder sb = new StringBuilder(cells.size());
        for (Cell c : cells) {
            if (c.isWideContinuation()) continue;
            sb.append(c.isEmpty() ? ' ' : c.getCharacter());
        }
        return sb.toString();
    }
    /**
     * If the cell at {@code col} is the left half of a wide char, clear the
     * continuation cell.  If it is a continuation cell, clear the left half.
     */
    private void clearWideNeighbour(int col) {
        Cell existing = cells.get(col);
        if (existing.isWide() && col + 1 < cells.size()) {
            Cell c = cells.get(col + 1);
            c.setCharacter(Cell.EMPTY_CHARACTER);
            c.setWideContinuation(false);
        } else if (existing.isWideContinuation() && col - 1 >= 0) {
            Cell c = cells.get(col - 1);
            c.setCharacter(Cell.EMPTY_CHARACTER);
            c.setWide(false);
        }
    }
    @Override
    public String toString() {

        return "Line[" + toPlainString() + "]";
    }
}
