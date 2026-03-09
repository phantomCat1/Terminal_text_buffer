package org.example;

import java.util.Objects;

public class Cell {
    private CellAttributes attributes;
    private char character;

    public static final Cell EMPTY = new Cell('\0', CellAttributes.DEFAULT);

    public Cell(char character, CellAttributes attributes) {
        this.character = character;
        this.attributes = attributes;
    }
    public boolean isEmpty(){
        return this.character == '\0';
    }
    // Basic getters and setters

    public char getCharacter() {
        return this.character;
    }
    public CellAttributes getAttributes() {
        return this.attributes;
    }

    public void setCharacter(char character) {
        this.character = character;
    }
    public void setAttributes(CellAttributes attributes) {
        this.attributes = attributes;
    }

    // Methods that might come in handy later
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell c)) return false;
        return character == c.character
                && attributes.equals(c.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, attributes);
    }

    @Override
    public String toString() {
        if (character == '\0') return "Cell{EMPTY}";
        return "Cell{'" + character + ", " + attributes + "}";
    }
}
