package org.example;

public enum TerminalColor {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),
    BRIGHT_BLACK(8),
    BRIGHT_RED(9),
    BRIGHT_GREEN(10),
    BRIGHT_YELLOW(11),
    BRIGHT_BLUE(12),
    BRIGHT_MAGENTA(13),
    BRIGHT_CYAN(14),
    BRIGHT_WHITE(15);

    private final int index;

    TerminalColor(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static TerminalColor fromIndex(int index) {
        for (TerminalColor c : values()) {
            if (c.index == index) return c;
        }
        throw new IllegalArgumentException("Invalid color index: " + index);
    }
}
