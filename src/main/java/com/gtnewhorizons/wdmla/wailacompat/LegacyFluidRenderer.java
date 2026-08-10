package com.gtnewhorizons.wdmla.wailacompat;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.config.General;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;

/** Builds modern fluid renderer tokens for legacy Waila tank providers. */
public final class LegacyFluidRenderer {

    private static final String EMPTY_FLUID = "EMPTYFLUID";
    private static final String[] MODERN_FLUID_STORAGE_TYPES = {
            "mods.railcraft.common.blocks.machine.beta.TileTankBase",
            "tconstruct.smeltery.logic.LavaTankLogic",
            "tconstruct.smeltery.logic.SmelteryLogic",
            "tconstruct.smeltery.logic.CastingChannelLogic",
            "codechicken.enderstorage.storage.liquid.TileEnderTank",
            "crazypants.enderio.machine.tank.TileTank",
            "gregtech.api.metatileentity.BaseMetaTileEntity",
            "gregtech.api.metatileentity.BaseMetaPipeEntity" };

    /** Prevents construction of the legacy fluid rendering helper. */
    private LegacyFluidRenderer() {}

    /** Checks whether a WDMla modern provider already renders this target's fluid storage. */
    public static boolean isHandledByModernProvider(IWailaDataAccessor accessor) {
        Object target = accessor.getTileEntity();
        return General.overrideWailaTooltips && isModernFluidStorage(target);
    }

    /** Detects only exact storage classes registered with WDMla's modern fluid providers. */
    public static boolean isModernFluidStorage(@Nullable Object target) {
        for (Class<?> type = target == null ? null : target.getClass(); type != null; type = type.getSuperclass()) {
            for (String storageType : MODERN_FLUID_STORAGE_TYPES) {
                if (storageType.equals(type.getName())) {
                    return true;
                }
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
