package com.gtnewhorizons.wdmla.plugin.bloodmagic;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import WayofTime.alchemicalWizardry.common.tileEntity.TETeleposer;
import WayofTime.alchemicalWizardry.common.tileEntity.TEWritingTable;

/** Absorbs the Blood Magic integration from WAILAPlugins. */
@WDMlaPlugin(uid = "blood_magic", dependencies = "AWWayofTime")
public class BloodMagicPlugin implements IWDMlaPlugin {

    /** Registers the conditional Blood Magic block renderer. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(BloodMagicProvider.INSTANCE, Block.class);
    }

    /** Registers every Blood Magic tile synchronized by WAILAPlugins. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerBlockDataProvider(BloodMagicProvider.INSTANCE, TEAltar.class);
        registration.registerBlockDataProvider(BloodMagicProvider.INSTANCE, TEWritingTable.class);
        registration.registerBlockDataProvider(BloodMagicProvider.INSTANCE, TEMasterStone.class);
        registration.registerBlockDataProvider(BloodMagicProvider.INSTANCE, TETeleposer.class);
    }
}
