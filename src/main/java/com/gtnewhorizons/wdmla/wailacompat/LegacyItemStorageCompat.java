package com.gtnewhorizons.wdmla.wailacompat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.config.General;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;

/** Removes legacy item rows only when an exact native item-storage view replaces them. */
public final class LegacyItemStorageCompat {

    private static final String GREGTECH_PROVIDER = "gregtech.crossmod.waila.GregtechTEWailaDataProvider";
    private static final String GREGTECH_DIGITAL_CHEST =
            "gregtech.common.tileentities.storage.MTEDigitalChestBase";
    private static final String GREGTECH_MORE_ITEMS_KEY = "GT5U.waila.machine.more_items";
    private static final String MORE_ITEMS_MARKER = "1357913579";
    private static final ConcurrentMap<Class<?>, Optional<Method>> META_TILE_GETTERS = new ConcurrentHashMap<>();

    /** Prevents construction of the legacy item compatibility filter. */
    private LegacyItemStorageCompat() {}

    /** Removes GregTech item enumeration rows only when the native item summary was actually rendered. */
    public static void filterBody(Object provider, IWailaDataAccessor accessor, int previousSize,
            List<String> tooltips, boolean nativeItemStorageRendered) {
        if (!General.overrideWailaTooltips || provider == null || accessor == null || tooltips == null
                || !shouldRemoveRenderedItemRows(provider.getClass().getName(), nativeItemStorageRendered)) {
            return;
        }

        NBTTagCompound data = accessor.getNBTData();
        if (data != null && data.hasKey("itemType", 10) && isGregTechDigitalChest(accessor.getTileEntity())) {
            int added = tooltips.size() - Math.max(0, previousSize);
            if (added >= 2) {
                tooltips.subList(tooltips.size() - 2, tooltips.size()).clear();
            }
        } else {
            removeRenderedItemRows(previousSize, tooltips);
        }
    }

    /** Checks that the matching GregTech provider has already produced a native item summary. */
    static boolean shouldRemoveRenderedItemRows(String providerClassName, boolean nativeItemStorageRendered) {
        return nativeItemStorageRendered && GREGTECH_PROVIDER.equals(providerClassName);
    }

    /** Removes legacy item-renderer rows while preserving headings, directions, and other machine details. */
    static void removeRenderedItemRows(int start, List<String> tooltips) {
        String moreItemsExample = StatCollector.translateToLocalFormatted(
                GREGTECH_MORE_ITEMS_KEY,
                MORE_ITEMS_MARKER);
        removeRenderedItemRows(start, tooltips, moreItemsExample);
    }

    /** Removes item rows and an adjacent localized more-items summary from the selected range. */
    static void removeRenderedItemRows(int start, List<String> tooltips, String moreItemsExample) {
        ListIterator<String> iterator = tooltips.listIterator(Math.max(0, Math.min(start, tooltips.size())));
        boolean followsItemRows = false;
        while (iterator.hasNext()) {
            String tooltip = iterator.next();
            if (tooltip == null) {
                followsItemRows = false;
                continue;
            }
            Matcher renderer = SpecialChars.patternRender.matcher(tooltip);
            boolean itemRow = false;
            while (renderer.find()) {
                if ("waila.stack".equalsIgnoreCase(renderer.group("name"))) {
                    itemRow = true;
                    break;
                }
            }
            if (itemRow || (followsItemRows && matchesMoreItemsLine(tooltip, moreItemsExample))) {
                iterator.remove();
                followsItemRows = true;
            } else {
                followsItemRows = false;
            }
        }
    }

    /** Matches the localized more-items text by replacing its unique numeric marker with the displayed count. */
    private static boolean matchesMoreItemsLine(String tooltip, String moreItemsExample) {
        int markerIndex = moreItemsExample.indexOf(MORE_ITEMS_MARKER);
        if (markerIndex < 0) {
            return false;
        }
        String prefix = moreItemsExample.substring(0, markerIndex);
        String suffix = moreItemsExample.substring(markerIndex + MORE_ITEMS_MARKER.length());
        return tooltip.startsWith(prefix)
                && tooltip.endsWith(suffix)
                && tooltip.length() > prefix.length() + suffix.length();
    }

    /** Resolves the wrapped meta tile with a per-wrapper-class reflection cache. */
    private static boolean isGregTechDigitalChest(Object target) {
        if (target == null) {
            return false;
        }
        Optional<Method> getter = META_TILE_GETTERS.computeIfAbsent(target.getClass(), LegacyItemStorageCompat::findGetter);
        if (!getter.isPresent()) {
            return false;
        }
        try {
            Object metaTile = getter.get().invoke(target);
            return hasType(metaTile, GREGTECH_DIGITAL_CHEST);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    /** Finds the public GregTech meta tile getter once for each wrapper implementation. */
    private static Optional<Method> findGetter(Class<?> targetClass) {
        try {
            return Optional.of(targetClass.getMethod("getMetaTileEntity"));
        } catch (NoSuchMethodException ignored) {
            return Optional.empty();
        }
    }

    /** Checks a class hierarchy by name without linking GregTech on installations where it is absent. */
    private static boolean hasType(Object target, String expectedClassName) {
        for (Class<?> type = target == null ? null : target.getClass(); type != null; type = type.getSuperclass()) {
            if (expectedClassName.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }
}
