package org.example;

import java.util.Objects;

public final class CellAttributes {
    private TerminalColor foregroundColor; // default value of null, since task description indicates that the 16 colors are extra(non-default)
    private TerminalColor backgroundColor; // default value of null, same as above
    private boolean bold;
    boolean italic;
    boolean underline;
    // attributes above could potentially be made final if users are not likely to change attributes frequently and to ensure immutability, thread safety etc
    // for now I will keep them non-final
    public static final CellAttributes DEFAULT = new CellAttributes(null, null, false, false, false);

    public CellAttributes(TerminalColor foregroundColor, TerminalColor backgroundColor, boolean bold, boolean italic, boolean underline) {
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }

    // Basic getters
    public TerminalColor getForegroundColor() {
        return this.foregroundColor;
    }
    public TerminalColor getBackgroundColor() {
        return this.backgroundColor;
    }
    public boolean isBold() {
        return this.bold;
    }
    public boolean isItalic() {
        return this.italic;
    }
    public boolean isUnderline() {
        return this.underline;
    }

    // Descriptive setters

    public void setForegroundColor(TerminalColor foregroundColor) {
        this.foregroundColor = foregroundColor;
    }
    public void setDefaultForegroundColor() {
        this.foregroundColor = null;
    }
    public void setBackgroundColor(TerminalColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public void setDefaultBackgroundColor() {
        this.backgroundColor = null;
    }
    public void setBold(boolean bold) {
        this.bold = bold;
    }
    public void setItalic(boolean italic) {
        this.italic = italic;
    }
    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    // Support methods. May come in handy later
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellAttributes a)) return false;
        return bold == a.bold
                && italic == a.italic
                && underline == a.underline
                && foregroundColor == a.foregroundColor
                && backgroundColor == a.backgroundColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(foregroundColor, backgroundColor, bold, italic, underline);
    }

    @Override
    public String toString() {
        return "CellAttributes{fg=" + foregroundColor
                + ", bg=" + backgroundColor
                + ", bold=" + bold
                + ", italic=" + italic
                + ", underline=" + underline + "}";
    }
}
