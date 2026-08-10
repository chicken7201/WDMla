package com.gtnewhorizons.wdmla.wailacompat;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.gtnewhorizons.wdmla.config.General;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;

/** Removes legacy fluid text when the same storage is rendered by WDMla's modern fluid view. */
public final class LegacyFluidStorageCompat {

    private static final String TCONSTRUCT_WAILA_PACKAGE = "tconstruct.plugins.waila.";
    private static final String ENDER_IO_WAILA_PROVIDER = "crazypants.enderio.waila.WailaCompat";
    private static final String ENDER_IO_TANK = "crazypants.enderio.machine.tank.TileTank";
    private static final String GREGTECH_WAILA_PROVIDER = "gregtech.crossmod.waila.GregtechTEWailaDataProvider";
    private static final String EMPTY_FLUID = "EMPTYFLUID";
    private static final Pattern FLUID_CAPACITY_TEXT = Pattern.compile(
            "(?i)^.*\\d[\\d,. ]*\\s*/\\s*\\d[\\d,. ]*\\s*mB\\s*$");

    /** Prevents construction of the legacy fluid compatibility filter. */
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
        if (ENDER_IO_WAILA_PROVIDER.equals(provider.getClass().getName()) && hasType(target, ENDER_IO_TANK)) {
            removeLastAddedRow(start, tooltips);
            return;
        }
        if (isStandardFluidStorage(target)) {
            // GregTech may replace an earlier body row instead of appending its fluid renderer.
            int fluidStart = GREGTECH_WAILA_PROVIDER.equals(provider.getClass().getName()) ? 0 : start;
            removeDuplicatedFluidRows(fluidStart, tooltips);
        }
    }

    /** Removes empty legacy fluid renderer rows unless the player is crouching. */
    public static void filterEmptyFluidRows(IWailaDataAccessor accessor, List<String> tooltips) {
        if (accessor == null || accessor.getPlayer() == null || accessor.getPlayer().isSneaking()
                || tooltips == null) {
            return;
        }

        ListIterator<String> iterator = tooltips.listIterator();
        while (iterator.hasNext()) {
            String tooltip = iterator.next();
            if (tooltip == null) {
                continue;
            }
            Matcher renderer = SpecialChars.patternRender.matcher(tooltip);
            StringBuffer filtered = new StringBuffer();
            boolean removed = false;
            while (renderer.find()) {
                if (isEmptyFluidRenderer(renderer)) {
                    renderer.appendReplacement(filtered, "");
                    removed = true;
                }
            }
            if (!removed) {
                continue;
            }

            renderer.appendTail(filtered);
            String visibleText = SpecialChars.patternMinecraft.matcher(filtered.toString()).replaceAll("");
            visibleText = SpecialChars.patternWaila.matcher(visibleText).replaceAll("").trim();
            if (visibleText.isEmpty()) {
                iterator.remove();
            } else {
                iterator.set(filtered.toString());
            }
        }
    }

    /** Detects the EMPTYFLUID sentinel in a legacy waila.fluid renderer token. */
    private static boolean isEmptyFluidRenderer(Matcher renderer) {
        if (!"waila.fluid".equalsIgnoreCase(renderer.group("name"))) {
            return false;
        }
        String arguments = renderer.group("args");
        if (arguments == null) {
            return false;
        }
        String[] values = arguments.split(Pattern.quote(SpecialChars.WailaRendererComma), -1);
        return values.length >= 2 && EMPTY_FLUID.equalsIgnoreCase(values[0])
                && EMPTY_FLUID.equalsIgnoreCase(values[1]);
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
                    removeDuplicatedFluidRows(start, tooltips);
                }
            }
        }
    }

    /** Removes legacy fluid text and waila.fluid tokens while preserving unrelated text on the same row. */
    private static void removeDuplicatedFluidRows(int start, List<String> tooltips) {
        ListIterator<String> iterator = tooltips.listIterator(start);
        while (iterator.hasNext()) {
            String tooltip = iterator.next();
            if (tooltip == null) {
                continue;
            }
            if (FLUID_CAPACITY_TEXT.matcher(tooltip).matches()) {
                iterator.remove();
                continue;
            }

            Matcher renderer = SpecialChars.patternRender.matcher(tooltip);
            StringBuffer filtered = new StringBuffer();
            boolean removedFluidRenderer = false;
            while (renderer.find()) {
                if ("waila.fluid".equalsIgnoreCase(renderer.group("name"))) {
                    renderer.appendReplacement(filtered, "");
                    removedFluidRenderer = true;
                }
            }
            if (!removedFluidRenderer) {
                continue;
            }

            renderer.appendTail(filtered);
            String visibleText = SpecialChars.patternMinecraft.matcher(filtered.toString()).replaceAll("");
            visibleText = SpecialChars.patternWaila.matcher(visibleText).replaceAll("").trim();
            if (visibleText.isEmpty()) {
                iterator.remove();
            } else {
                iterator.set(filtered.toString());
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

    /** Detects storage targets covered by WDMla's modern fluid providers. */
    private static boolean isStandardFluidStorage(Object target) {
        return LegacyFluidRenderer.isModernFluidStorage(target);
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

    /** Checks an optional target hierarchy by name without linking the owning mod. */
    private static boolean hasType(Object target, String expectedClassName) {
        for (Class<?> type = target == null ? null : target.getClass(); type != null; type = type.getSuperclass()) {
            if (expectedClassName.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    /** Removes the final row appended by a provider while preserving its unrelated setup rows. */
    private static void removeLastAddedRow(int start, List<String> tooltips) {
        if (tooltips.size() > start) {
            tooltips.remove(tooltips.size() - 1);
        }
    }

    /** Removes every row appended by the current legacy provider. */
    private static void clearAddedRows(int start, List<String> tooltips) {
        tooltips.subList(start, tooltips.size()).clear();
    }
}
