package com.gtnewhorizons.wdmla.plugin.railcraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.CommonProxy;
import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.beta.TileTankBase;

/** Exposes every formed Railcraft multiblock fluid handler through WDMla's standard gauges. */
public enum RailcraftMultiBlockFluidProvider implements IServerExtensionProvider<FluidView.Data> {

    INSTANCE;

    /** Collects every valid tank from the authoritative master for any targeted structure part. */
    @Nullable
    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
        Object target = accessor.getTarget();
        if (!(target instanceof TileMultiBlock)) {
            return null;
        }
        if (target instanceof TileTankBase) {
            return null;
        }

        IFluidHandler fluidHandler = RailcraftMultiBlockSupport.findFluidHandler(accessor);
        if (fluidHandler == null) {
            return Collections.emptyList();
        }
        FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(ForgeDirection.UNKNOWN);
        if (tankInfo == null || tankInfo.length == 0) {
            return Collections.emptyList();
        }

        FluidTankInfo[] validTanks = Arrays.stream(tankInfo).filter(info -> info != null && info.capacity > 0)
                .toArray(FluidTankInfo[]::new);
        if (validTanks.length == 0) {
            return Collections.emptyList();
        }
        return CommonProxy.fromFluidStorage(validTanks);
    }

    /** Reuses WDMla's standard fluid-storage packet and client gauge renderer. */
    @Override
    public net.minecraft.util.ResourceLocation getUid() {
        return Identifiers.FLUID_STORAGE_DEFAULT;
    }
}
