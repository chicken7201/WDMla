package com.gtnewhorizons.wdmla.wailacompat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class LegacyGregTechTooltipCompatTest {

    /** Verifies only Input is removed while its contents and the directional Output heading remain. */
    @Test
    void removesOnlyInputHeading() {
        List<String> tooltips = new ArrayList<>(
                Arrays.asList("Stored Energy", "Input:", "1x Programmed Circuit", "Output: North", "Facing: South"));

        LegacyGregTechTooltipCompat.removeInputHeading(1, tooltips, "Input:");

        assertEquals(
                Arrays.asList("Stored Energy", "1x Programmed Circuit", "Output: North", "Facing: South"),
                tooltips);
    }

    /** Verifies similarly named rows belonging to earlier providers stay untouched. */
    @Test
    void searchesOnlyNewlyAppendedRows() {
        List<String> tooltips = new ArrayList<>(
                Arrays.asList("Input:", "Other provider", "Input:", "Output: West"));

        LegacyGregTechTooltipCompat.removeInputHeading(2, tooltips, "Input:");

        assertEquals(Arrays.asList("Input:", "Other provider", "Output: West"), tooltips);
    }
}
