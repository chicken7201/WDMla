package com.gtnewhorizons.wdmla.plugin.tconstruct;

import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mcp.mobius.waila.Waila;

/** Registers modern fluid storage views for TConstruct's multi-tank blocks. */
@WDMlaPlugin(uid = "tconstruct", dependencies = "TConstruct")
public class TConstructPlugin implements IWDMlaPlugin {

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
