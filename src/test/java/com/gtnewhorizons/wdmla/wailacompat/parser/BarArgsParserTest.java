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

    /** Verifies process progress continues at the measured rate between server samples. */
    @Test
    void predictsProcessRatioBetweenSamples() {
        BarArgsParser parser = new BarArgsParser();
        Object target = new Object();
        Object firstSnapshot = new Object();
        Object secondSnapshot = new Object();

        assertEquals(0.2, parser.updateInterpolatedProcessRatio(0.2, target, firstSnapshot, true, 1_000_000_000L));
        assertEquals(
                0.3,
                parser.updateInterpolatedProcessRatio(0.3, target, secondSnapshot, true, 1_100_000_000L),
                0.000_001);
        assertEquals(
                0.35,
                parser.updateInterpolatedProcessRatio(0.3, target, secondSnapshot, true, 1_150_000_000L),
                0.000_001);
    }

    /** Verifies a stopped or restarted process never continues extrapolating stale progress. */
    @Test
    void stopsPredictionForInactiveAndResetProcesses() {
        BarArgsParser parser = new BarArgsParser();
        Object target = new Object();
        Object firstSnapshot = new Object();
        Object secondSnapshot = new Object();

        parser.updateInterpolatedProcessRatio(0.2, target, firstSnapshot, true, 1_000_000_000L);
        parser.updateInterpolatedProcessRatio(0.3, target, secondSnapshot, true, 1_100_000_000L);

        assertEquals(
                0.3,
                parser.updateInterpolatedProcessRatio(0.3, target, secondSnapshot, false, 1_150_000_000L),
                0.000_001);
        assertEquals(
                0.05,
                parser.updateInterpolatedProcessRatio(0.05, target, new Object(), true, 1_200_000_000L),
                0.000_001);
    }
}
