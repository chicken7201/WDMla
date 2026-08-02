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

    private LegacyFluidRenderer() {}

    /** Checks whether WDMla's generic provider already renders this target's fluid storage. */
    public static boolean isHandledByModernProvider(IWailaDataAccessor accessor) {
        Object target = accessor.getTileEntity();
        return General.overrideWailaTooltips && (target instanceof IFluidHandler || target instanceof IFluidTank);
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
