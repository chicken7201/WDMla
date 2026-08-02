package com.gtnewhorizons.wdmla.plugin.tconstruct;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mcp.mobius.waila.Waila;

/** Registers modern fluid storage views for TConstruct's multi-tank blocks. */
@WDMlaPlugin(uid = "tconstruct", dependencies = "TConstruct")
public class TConstructPlugin implements IWDMlaPlugin {

    /** Registers WAWLA's TConstruct block information as native WDMla components. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(TConstructWawlaProvider.INSTANCE, Block.class);
    }

    /** Registers exact tile classes so their multi-tank providers win over the generic fallback. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registerFluidProvider(
                registration,
                CastingChannelFluidStorageProvider.INSTANCE,
                "tconstruct.smeltery.logic.CastingChannelLogic");
        registerFluidProvider(
                registration,
                SmelteryFluidStorageProvider.INSTANCE,
                "tconstruct.smeltery.logic.SmelteryLogic");
        registerDataProvider(registration, "tconstruct.blocks.logic.DryingRackLogic");
        registerDataProvider(registration, "tconstruct.tools.logic.FurnaceLogic");
        registerDataProvider(registration, "tconstruct.mechworks.blocks.BlockLandmine");
    }

    /** Resolves WAWLA-supported TConstruct classes and registers their synchronized data provider. */
    private static void registerDataProvider(IWDMlaCommonRegistration registration, String className) {
        try {
            registration.registerBlockDataProvider(TConstructWawlaProvider.INSTANCE, Class.forName(className));
        } catch (ClassNotFoundException e) {
            Waila.log.debug("TConstruct WAWLA target {} is not present in this TConstruct build", className);
        }
    }

    /** Resolves an optional TConstruct tile class without adding a compile-time dependency. */
    private static void registerFluidProvider(IWDMlaCommonRegistration registration,
            TConstructFluidStorageProvider provider, String className) {
        try {
            registration.registerFluidStorage(provider, Class.forName(className));
        } catch (ClassNotFoundException e) {
            Waila.log.warn("Unable to register TConstruct fluid storage compatibility for {}", className, e);
        }
    }
}
