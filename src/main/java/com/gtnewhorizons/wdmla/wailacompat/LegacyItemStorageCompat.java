package com.gtnewhorizons.wdmla.wailacompat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.wdmla.config.General;

import mcp.mobius.waila.api.IWailaDataAccessor;

/** Removes legacy item rows only when an exact native item-storage view replaces them. */
public final class LegacyItemStorageCompat {

    private static final String GREGTECH_PROVIDER = "gregtech.crossmod.waila.GregtechTEWailaDataProvider";
    private static final String GREGTECH_DIGITAL_CHEST =
            "gregtech.common.tileentities.storage.MTEDigitalChestBase";
    private static final ConcurrentMap<Class<?>, Optional<Method>> META_TILE_GETTERS = new ConcurrentHashMap<>();

    /** Prevents construction of the legacy item compatibility filter. */
    private LegacyItemStorageCompat() {}

    /** Removes GregTech's count and type rows when the capacity-aware native row is available. */
    public static void filterBody(Object provider, IWailaDataAccessor accessor, int previousSize,
            List<String> tooltips) {
        if (!General.overrideWailaTooltips || provider == null || accessor == null || tooltips == null
                || !GREGTECH_PROVIDER.equals(provider.getClass().getName())) {
            return;
        }

        NBTTagCompound data = accessor.getNBTData();
        if (data == null || !data.hasKey("itemType", 10) || !isGregTechDigitalChest(accessor.getTileEntity())) {
            return;
        }

        int added = tooltips.size() - Math.max(0, previousSize);
        if (added >= 2) {
            tooltips.subList(tooltips.size() - 2, tooltips.size()).clear();
        }
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
