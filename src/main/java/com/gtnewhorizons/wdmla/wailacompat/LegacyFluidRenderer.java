package com.gtnewhorizons.wdmla.wailacompat;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.config.General;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;

/** Builds modern fluid renderer tokens for legacy Waila tank providers. */
public final class LegacyFluidRenderer {

    private static final String EMPTY_FLUID = "EMPTYFLUID";
    private static final String RAILCRAFT_TANK_TILE = "mods.railcraft.common.blocks.machine.beta.TileTankBase";

    private LegacyFluidRenderer() {}

    /** Checks whether a WDMla modern provider already renders this target's fluid storage. */
    public static boolean isHandledByModernProvider(IWailaDataAccessor accessor) {
        Object target = accessor.getTileEntity();
        return General.overrideWailaTooltips && isModernFluidStorage(target);
    }

    /** Detects Forge tanks and Railcraft multiblock parts handled by WDMla's modern fluid providers. */
    public static boolean isModernFluidStorage(@Nullable Object target) {
        if (target instanceof IFluidHandler || target instanceof IFluidTank) {
            return true;
        }
        for (Class<?> type = target == null ? null : target.getClass(); type != null; type = type.getSuperclass()) {
            if (RAILCRAFT_TANK_TILE.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    /** Encodes one fluid tank as a binary-compatible waila.fluid renderer token. */
    public static String render(@Nullable FluidStack fluid, int amount, int capacity) {
        String registryName = EMPTY_FLUID;
        String localizedName = EMPTY_FLUID;
        if (fluid != null && fluid.getFluid() != null) {
            registryName = fluid.getFluid().getName();
            localizedName = fluid.getFluid().getLocalizedName(fluid);
        }
        return SpecialChars.getRenderString(
                "waila.fluid",
                registryName,
                localizedName,
                Integer.toString(Math.max(0, amount)),
                Integer.toString(Math.max(0, capacity)));
    }
}
