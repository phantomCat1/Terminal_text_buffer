package org.example;

import java.util.Objects;

public class Cell {
    private CellAttributes attributes;
    private char character;
    private boolean wide;
    private boolean wideContinuation;
    public static final char EMPTY_CHARACTER = '\0';
    public static final Cell EMPTY = new Cell(EMPTY_CHARACTER, CellAttributes.DEFAULT, false, false);

    public Cell(char character, CellAttributes attributes, boolean wide, boolean wideContinuation) {
        if (wide && wideContinuation) {
            throw new IllegalArgumentException("A cell cannot be both wide and wideContinuation");
        }
        this.character = character;
        this.attributes = attributes;
        this.wide = wide;
        this.wideContinuation = wideContinuation;
    }

    public Cell(char character, CellAttributes attributes) {
        this(character, attributes, false, false);
    }

    public Cell(char character) {
        this(character, CellAttributes.DEFAULT, false, false);
    }

    public boolean isWide(){
        return this.wide;
    }

    public boolean isWideContinuation(){
        return this.wideContinuation;
    }

    public void setWide(boolean wide){
        this.wide = wide;
    }

    public void setWideContinuation(boolean wideContinuation){
        this.wideContinuation = wideContinuation;
    }

    public boolean isEmpty(){
        return this.character == EMPTY_CHARACTER && !wide;
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
        if (attributes == null && c.attributes == null) {
            return character == c.character;
        } else if (attributes == null) return false;

        return character == c.character
                && wide == c.wide
                && wideContinuation == c.wideContinuation
                && attributes.equals(c.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, attributes, wide, wideContinuation);
    }

    @Override
    public String toString() {
        if (wideContinuation) return "Cell{WIDE_CONTINUATION}";
        if (character == EMPTY_CHARACTER) return "Cell{EMPTY}";
        return "Cell{" + character + ", " +(wide ? "wide, " : "") + attributes + "}";
    }
}
