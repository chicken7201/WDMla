package com.gtnewhorizons.wdmla.plugin.railcraft;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fluids.FluidTankInfo;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.CommonProxy;
import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.beta.TileTankBase;
import mods.railcraft.common.fluids.tanks.StandardTank;

/** Converts Railcraft multiblock tanks into WDMla's standard textured fluid gauge. */
public enum RailcraftTankFluidProvider implements IServerExtensionProvider<FluidView.Data> {

    INSTANCE;

    /** Resolves the tank master and exposes its fluid through the standard FluidView encoder. */
    @Nullable
    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
        Object target = accessor.getTarget();
        if (!(target instanceof TileTankBase tankTile)) {
            return null;
        }
        if (!tankTile.isStructureValid()) {
            return Collections.emptyList();
        }
        TileMultiBlock multiBlock = tankTile;
        if (multiBlock.getMasterBlock() instanceof TileTankBase master) {
            tankTile = master;
        }
        StandardTank tank = tankTile.getTank();
        if (tank == null) {
            return Collections.emptyList();
        }
        FluidTankInfo tankInfo = tank.getInfo();
        if (tankInfo == null || tankInfo.fluid == null || tankInfo.fluid.amount <= 0) {
            return Collections.emptyList();
        }
        return CommonProxy.fromFluidStorage(new FluidTankInfo[] { tankInfo });
    }

    /** Uses the built-in FluidView client decoder. */
    @Override
    public net.minecraft.util.ResourceLocation getUid() {
        return Identifiers.FLUID_STORAGE_DEFAULT;
    }

    /** Makes this exact Railcraft provider win over the generic Forge handler. */
    @Override
    public int getDefaultPriority() {
        return 900;
    }
}
