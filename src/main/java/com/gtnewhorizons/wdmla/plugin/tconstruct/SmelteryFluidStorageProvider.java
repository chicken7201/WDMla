package com.gtnewhorizons.wdmla.plugin.tconstruct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fluids.FluidTankInfo;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.CommonProxy;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

/** Exposes every molten fluid in a TConstruct smeltery as an individual gauge. */
public enum SmelteryFluidStorageProvider implements TConstructFluidStorageProvider {

    INSTANCE;

    /** Calls TConstruct's public multi-tank API while keeping TConstruct an optional dependency. */
    @Nullable
    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
        Object target = accessor.getTarget();
        if (target == null) {
            return null;
        }
        try {
            Method getMultiTankInfo = target.getClass().getMethod("getMultiTankInfo");
            FluidTankInfo[] tanks = (FluidTankInfo[]) getMultiTankInfo.invoke(target);
            if (tanks == null) {
                return null;
            }
            List<FluidTankInfo> visibleTanks = new ArrayList<>();
            FluidTankInfo emptyCapacity = null;
            for (FluidTankInfo tank : tanks) {
                if (tank != null && tank.fluid != null) {
                    visibleTanks.add(tank);
                } else if (tank != null && tank.capacity > 0) {
                    emptyCapacity = tank;
                }
            }
            if (visibleTanks.isEmpty() && emptyCapacity != null) {
                visibleTanks.add(emptyCapacity);
            }
            return CommonProxy.fromFluidStorage(visibleTanks.toArray(new FluidTankInfo[0]));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to read TConstruct smeltery fluid storage", e);
        }
    }
}
