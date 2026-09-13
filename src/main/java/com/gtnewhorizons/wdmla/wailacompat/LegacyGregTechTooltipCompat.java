package com.gtnewhorizons.wdmla.wailacompat;

import java.util.List;
import java.util.ListIterator;

import net.minecraft.util.StatCollector;

/** Applies narrowly scoped cleanup to legacy GregTech machine tooltip rows. */
public final class LegacyGregTechTooltipCompat {

    static final String GREGTECH_TILE_PROVIDER = "gregtech.crossmod.waila.GregtechTEWailaDataProvider";
    private static final String INPUT_HEADING_KEY = "GT5U.waila.machine.input";

    /** Prevents construction of the stateless compatibility filter. */
    private LegacyGregTechTooltipCompat() {}

    /** Removes only the Input heading appended by GregTech while preserving all contents and Output direction. */
    public static void filterBody(Object provider, int previousSize, List<String> tooltips) {
        if (provider == null
                || tooltips == null
                || !GREGTECH_TILE_PROVIDER.equals(provider.getClass().getName())) {
            return;
        }

        removeInputHeading(
                Math.max(0, Math.min(previousSize, tooltips.size())),
                tooltips,
                StatCollector.translateToLocal(INPUT_HEADING_KEY));
    }

    /** Removes the first exact Input heading in the newly appended row range. */
    static void removeInputHeading(int start, List<String> tooltips, String inputHeading) {
        ListIterator<String> iterator = tooltips.listIterator(Math.max(0, Math.min(start, tooltips.size())));
        while (iterator.hasNext()) {
            if (inputHeading.equals(iterator.next())) {
                iterator.remove();
                return;
            }
        }
    }
}
