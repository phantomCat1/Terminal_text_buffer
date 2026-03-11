package org.example;

/** Create using Claude
 * Utility methods for Unicode "wide" character detection.
 * <p>
 * A character is considered "wide" (occupying 2 terminal columns) when its
 * Unicode East Asian Width property is <em>Wide</em> or <em>Fullwidth</em>.
 * This covers:
 * <ul>
 *   <li>CJK Unified Ideographs and extensions</li>
 *   <li>CJK Compatibility Ideographs</li>
 *   <li>Hangul syllables</li>
 *   <li>Katakana / Hiragana fullwidth forms</li>
 *   <li>Many emoji in the Miscellaneous Symbols and Pictographs block</li>
 * </ul>
 * The implementation uses range checks that cover the most common wide
 * blocks without requiring an external Unicode data library.
 */
public final class WideCharUtil {

    private WideCharUtil() {}

    /**
     * Returns {@code true} if {@code ch} occupies 2 terminal columns.
     */
    public static boolean isWide(char ch) {
        int cp = ch; // char is always a BMP code-point (U+0000 – U+FFFF)
        return isWideCodePoint(cp);
    }

    /**
     * Returns {@code true} if the Unicode code-point {@code cp} occupies 2
     * terminal columns.
     */
    public static boolean isWideCodePoint(int cp) {
        // Fullwidth Latin / Katakana / CJK punctuation (U+FF01 – U+FF60, U+FFE0 – U+FFE6)
        if (cp >= 0xFF01 && cp <= 0xFF60) return true;
        if (cp >= 0xFFE0 && cp <= 0xFFE6) return true;

        // CJK Unified Ideographs (U+4E00 – U+9FFF and extensions)
        if (cp >= 0x4E00 && cp <= 0x9FFF) return true;
        if (cp >= 0x3400 && cp <= 0x4DBF) return true;  // Extension A
        if (cp >= 0x20000 && cp <= 0x2A6DF) return true; // Extension B (supplementary)

        // CJK Compatibility Ideographs (U+F900 – U+FAFF)
        if (cp >= 0xF900 && cp <= 0xFAFF) return true;

        // Hangul Syllables (U+AC00 – U+D7A3)
        if (cp >= 0xAC00 && cp <= 0xD7A3) return true;

        // Hiragana (U+3040 – U+309F)
        if (cp >= 0x3040 && cp <= 0x309F) return true;

        // Katakana (U+30A0 – U+30FF)
        if (cp >= 0x30A0 && cp <= 0x30FF) return true;

        // Bopomofo and CJK Radicals (U+2E80 – U+33FF)
        if (cp >= 0x2E80 && cp <= 0x33FF) return true;

        // Enclosed CJK Letters and Months (U+3200 – U+32FF) – already in above range

        // Miscellaneous Symbols and Pictographs / Emoticons (U+1F300 – U+1F9FF)
        if (cp >= 0x1F300 && cp <= 0x1F9FF) return true;

        // Transport and Map Symbols (U+1F680 – U+1F6FF) – already in above range

        return false;
    }

    /**
     * Returns the display width of a character: 2 for wide, 1 for everything
     * else (including control characters – callers should handle those
     * separately).
     */
    public static int displayWidth(char ch) {
        return isWide(ch) ? 2 : 1;
    }
}
