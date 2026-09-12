package com.gtnewhorizons.wdmla.wailacompat.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BarArgsParserTest {

    /** Verifies signed GregTech ARGB values are retained and malformed values use the requested fallback. */
    @Test
    void parsesOptionalColors() {
        String[] args = { "Energy", "-4523", "invalid" };

        assertEquals(-4523, BarArgsParser.parseColor(args, 1, BarArgsParser.DEFAULT_TOP_COLOR));
        assertEquals(
                BarArgsParser.DEFAULT_BOTTOM_COLOR,
                BarArgsParser.parseColor(args, 2, BarArgsParser.DEFAULT_BOTTOM_COLOR));
        assertEquals(123, BarArgsParser.parseColor(args, 3, 123));
    }

    /** Verifies ratios are clamped and non-finite values cannot leak into the progress drawable. */
    @Test
    void clampsOptionalRatio() {
        assertEquals(0.25f, BarArgsParser.parseRatio(new String[] { "", "", "", "0.25" }, 3));
        assertEquals(0.0f, BarArgsParser.parseRatio(new String[] { "", "", "", "-2" }, 3));
        assertEquals(1.0f, BarArgsParser.parseRatio(new String[] { "", "", "", "2" }, 3));
        assertEquals(0.0f, BarArgsParser.parseRatio(new String[] { "", "", "", "NaN" }, 3));
        assertEquals(1.0f, BarArgsParser.parseRatio(new String[] { "" }, 3));
    }
}
