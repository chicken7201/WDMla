package com.gtnewhorizons.wdmla.wailacompat.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BarArgsParserTest {

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
