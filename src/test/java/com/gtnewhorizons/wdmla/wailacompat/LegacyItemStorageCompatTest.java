package com.gtnewhorizons.wdmla.wailacompat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import mcp.mobius.waila.api.SpecialChars;

class LegacyItemStorageCompatTest {

    /** Verifies lower GregTech item rows are filtered only when the native summary was actually rendered. */
    @Test
    void requiresRenderedNativeSummary() {
        assertFalse(
                LegacyItemStorageCompat.shouldRemoveRenderedItemRows(
                        "gregtech.crossmod.waila.GregtechTEWailaDataProvider",
                        false));
        assertTrue(
                LegacyItemStorageCompat.shouldRemoveRenderedItemRows(
                        "gregtech.crossmod.waila.GregtechTEWailaDataProvider",
                        true));
        assertFalse(LegacyItemStorageCompat.shouldRemoveRenderedItemRows("another.Provider", true));
    }

    /** Verifies only item enumeration rows are removed and the directional Output row remains. */
    @Test
    void removesItemRowsButPreservesOutputDirection() {
        String stack = SpecialChars.getRenderString("waila.stack", "serialized-item");
        List<String> tooltips = new ArrayList<>(
                Arrays.asList(
                        "Stored Energy",
                        stack + "64x Wheat",
                        "And 2 more...",
                        "Output: North",
                        stack + "12x Powder"));

        LegacyItemStorageCompat.removeRenderedItemRows(1, tooltips, "And 1357913579 more...");

        assertEquals(Arrays.asList("Stored Energy", "Output: North"), tooltips);
    }
}
