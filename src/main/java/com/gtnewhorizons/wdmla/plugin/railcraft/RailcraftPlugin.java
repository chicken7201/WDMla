package com.gtnewhorizons.wdmla.plugin.railcraft;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mods.railcraft.common.blocks.machine.TileMachineBase;
import mods.railcraft.common.blocks.machine.beta.TileTankBase;
import mods.railcraft.common.blocks.tracks.TileTrack;
import mods.railcraft.common.carts.EntityLocomotive;

/** Absorbs the Railcraft integration from WAILAPlugins. */
@WDMlaPlugin(uid = "railcraft_details", dependencies = "Railcraft")
public class RailcraftPlugin implements IWDMlaPlugin {

    /** Registers Railcraft block and locomotive renderers. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(RailcraftDetailsProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(RailcraftLocomotiveProvider.INSTANCE, EntityLocomotive.class);
    }

    /** Registers Railcraft machine, track, tank, and locomotive server providers. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerBlockDataProvider(RailcraftDetailsProvider.INSTANCE, TileMachineBase.class);
        registration.registerBlockDataProvider(RailcraftDetailsProvider.INSTANCE, TileTrack.class);
        registration.registerEntityDataProvider(RailcraftLocomotiveProvider.INSTANCE, EntityLocomotive.class);
        registration.registerFluidStorage(RailcraftTankFluidProvider.INSTANCE, TileTankBase.class);
    }
}
