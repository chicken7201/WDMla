package com.gtnewhorizons.wdmla.plugin.ic2;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import ic2.api.crops.ICropTile;

/** Absorbs the IC2 crop-card integration from WAILAPlugins. */
@WDMlaPlugin(uid = "ic2_crops", dependencies = "IC2", excludedDependencies = "cropsnh")
public class IC2CropsPlugin implements IWDMlaPlugin {

    /** Registers the conditional IC2 crop renderer for blocks. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(IC2CropProvider.INSTANCE, Block.class);
    }

    /** Registers server data collection for all IC2 crop tiles. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerBlockDataProvider(IC2CropProvider.INSTANCE, ICropTile.class);
    }
}
