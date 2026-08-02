package com.gtnewhorizons.wdmla.plugin.tconstruct;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.CommonProxy;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

/** Exposes the casting channel's internal and four directional subtanks as fluid gauges. */
public enum CastingChannelFluidStorageProvider implements TConstructFluidStorageProvider {

    INSTANCE;

    private static final ForgeDirection[] TANK_DIRECTIONS = { ForgeDirection.UNKNOWN, ForgeDirection.NORTH,
            ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST };

    /** Collects every casting channel tank instead of only the UNKNOWN-side tank. */
    @Nullable
    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
        if (!(accessor.getTarget() instanceof IFluidHandler fluidHandler)) {
            return null;
        }
        List<FluidTankInfo> tanks = new ArrayList<>();
        for (ForgeDirection direction : TANK_DIRECTIONS) {
            FluidTankInfo[] directionTanks = fluidHandler.getTankInfo(direction);
            if (directionTanks != null) {
                for (FluidTankInfo tank : directionTanks) {
                    if (tank != null) {
                        tanks.add(tank);
                    }
                }
            }
        }
        return CommonProxy.fromFluidStorage(tanks.toArray(new FluidTankInfo[0]));
    }
}
