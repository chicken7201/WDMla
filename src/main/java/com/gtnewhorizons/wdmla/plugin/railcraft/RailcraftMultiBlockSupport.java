package com.gtnewhorizons.wdmla.plugin.railcraft;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.api.accessor.Accessor;

import mods.railcraft.common.blocks.machine.ITankTile;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.alpha.TileSteamTurbine;

/** Resolves storage owned by a formed Railcraft multiblock's master tile. */
final class RailcraftMultiBlockSupport {

    /** Prevents construction of the shared Railcraft storage resolver. */
    private RailcraftMultiBlockSupport() {}

    /** Resolves a formed target part to its authoritative multiblock master. */
    @Nullable
    static TileMultiBlock resolveMaster(Accessor accessor) {
        if (!(accessor.getTarget() instanceof TileMultiBlock target) || !target.isStructureValid()) {
            return null;
        }
        if (target.isMaster()) {
            return target;
        }
        return target.getMasterBlock();
    }

    /** Finds the inventory exposed by the master or its functional component. */
    @Nullable
    static IInventory findInventory(Accessor accessor) {
        TileMultiBlock master = resolveMaster(accessor);
        if (master == null) {
            return null;
        }

        IInventory inventory = findDirectInventory(master);
        if (inventory != null) {
            return inventory;
        }
        for (TileEntity component : master.getComponents()) {
            if (component instanceof TileMultiBlock multiBlock) {
                inventory = findDirectInventory(multiBlock);
                if (inventory != null) {
                    return inventory;
                }
            }
        }
        return null;
    }

    /** Finds a Forge fluid handler on the master or its functional component. */
    @Nullable
    static IFluidHandler findFluidHandler(Accessor accessor) {
        TileMultiBlock master = resolveMaster(accessor);
        if (master == null) {
            return null;
        }
        if (master instanceof IFluidHandler fluidHandler) {
            return fluidHandler;
        }
        for (TileEntity component : master.getComponents()) {
            if (component instanceof IFluidHandler fluidHandler) {
                return fluidHandler;
            }
        }
        return null;
    }

    /** Reads each Railcraft multiblock inventory API without reflective field access. */
    @Nullable
    private static IInventory findDirectInventory(TileMultiBlock tile) {
        if (tile instanceof IInventory inventory) {
            return inventory;
        }
        if (tile instanceof ITankTile tankTile) {
            return tankTile.getInventory();
        }
        if (tile instanceof TileSteamTurbine turbine) {
            return turbine.getInventory();
        }
        return null;
    }
}
