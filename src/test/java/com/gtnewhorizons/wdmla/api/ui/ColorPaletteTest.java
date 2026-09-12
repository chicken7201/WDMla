package com.gtnewhorizons.wdmla.api.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ColorPaletteTest {

    /** Verifies new configurations use the requested solid green Process Fill colour. */
    @Test
    void progressFillDefaultsToSolidGreen() {
        assertEquals(0xFF00FF00, ColorPalette.PROGRESS_FILLED);
        assertEquals(ColorPalette.PROGRESS_FILLED, ColorPalette.PROGRESS_FILLED_ALTERNATE);
    }
}
