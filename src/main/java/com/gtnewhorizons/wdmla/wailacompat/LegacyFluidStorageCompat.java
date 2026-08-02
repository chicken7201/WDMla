package com.gtnewhorizons.wdmla.wailacompat;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import com.gtnewhorizons.wdmla.config.General;

/** Removes legacy fluid text when the same storage is rendered by WDMla's modern fluid view. */
public final class LegacyFluidStorageCompat {

    private static final String TCONSTRUCT_WAILA_PACKAGE = "tconstruct.plugins.waila.";
    private static final Pattern FLUID_CAPACITY_TEXT = Pattern.compile(
            "(?i)^.*\\d[\\d,. ]*\\s*/\\s*\\d[\\d,. ]*\\s*mB\\s*$");

    private LegacyFluidStorageCompat() {}

    /** Filters fluid-only lines appended by one legacy body provider. */
    public static void filterBody(Object provider, Object target, int previousSize, List<String> tooltips) {
        if (!General.overrideWailaTooltips || provider == null || target == null || tooltips == null) {
            return;
        }
        int start = Math.max(0, Math.min(previousSize, tooltips.size()));
        if (provider.getClass().getName().startsWith(TCONSTRUCT_WAILA_PACKAGE)) {
            filterTConstruct(provider.getClass().getSimpleName(), target, start, tooltips);
            return;
        }
        if (isStandardFluidStorage(target)) {
            tooltips.subList(start, tooltips.size()).removeIf(FLUID_CAPACITY_TEXT.asPredicate());
        }
    }

    /** Removes the known Liquid/Amount rows emitted by GTNH TConstruct's Waila providers. */
    private static void filterTConstruct(String providerName, Object target, int start, List<String> tooltips) {
        switch (providerName) {
            case "SearedTankDataProvider", "CastingChannelDataProvider" -> clearAddedRows(start, tooltips);
            case "BasinDataProvider" -> filterCastingRows(target, start, tooltips, false);
            case "TableDataProvider" -> filterCastingRows(target, start, tooltips, true);
            case "SmelteryDataProvider" -> {
                if (isValidTConstructSmeltery(target)) {
                    clearAddedRows(start, tooltips);
                }
            }
            default -> {
                if (isStandardFluidStorage(target)) {
                    tooltips.subList(start, tooltips.size()).removeIf(FLUID_CAPACITY_TEXT.asPredicate());
                }
            }
        }
    }

    /** Keeps casting progress and item text while removing the two fluid text rows. */
    private static void filterCastingRows(Object target, int start, List<String> tooltips, boolean mayContainItemRow) {
        if (!(target instanceof IFluidHandler fluidHandler) || !hasFluid(fluidHandler)) {
            return;
        }
        int added = tooltips.size() - start;
        int fluidStart = mayContainItemRow && added > 3 ? start + 1 : start;
        int fluidEnd = Math.min(fluidStart + 2, tooltips.size());
        if (fluidStart < fluidEnd) {
            tooltips.subList(fluidStart, fluidEnd).clear();
        }
    }

    /** Checks whether a Forge fluid handler currently contains fluid. */
    private static boolean hasFluid(IFluidHandler fluidHandler) {
        FluidTankInfo[] tanks = fluidHandler.getTankInfo(ForgeDirection.UNKNOWN);
        return tanks != null && tanks.length > 0 && tanks[0] != null && tanks[0].fluid != null
                && tanks[0].fluid.amount > 0;
    }

    /** Detects storage targets covered by WDMla's generic Forge fluid provider. */
    private static boolean isStandardFluidStorage(Object target) {
        return target instanceof IFluidHandler || target instanceof IFluidTank;
    }

    /** Reads TConstruct's public structure state without introducing a hard dependency on the mod. */
    private static boolean isValidTConstructSmeltery(Object target) {
        try {
            Field validStructure = target.getClass().getField("validStructure");
            return validStructure.getBoolean(target);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    /** Removes every row appended by the current legacy provider. */
    private static void clearAddedRows(int start, List<String> tooltips) {
        tooltips.subList(start, tooltips.size()).clear();
    }
}
