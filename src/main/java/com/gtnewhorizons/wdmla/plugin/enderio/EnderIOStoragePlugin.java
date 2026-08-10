package com.gtnewhorizons.wdmla.plugin.enderio;

import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.plugin.universal.FluidStorageProvider;

import mcp.mobius.waila.Waila;

/** Adds a capacity-aware view only for Ender IO's dedicated fluid Tank. */
@WDMlaPlugin(uid = "enderio_storage", dependencies = "EnderIO")
public class EnderIOStoragePlugin implements IWDMlaPlugin {

    private static final String TILE_TANK = "crazypants.enderio.machine.tank.TileTank";

    /** Registers the exact Tank tile without exposing fluid buffers from other Ender IO machines. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        try {
            registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, Class.forName(TILE_TANK));
        } catch (ClassNotFoundException exception) {
            Waila.log.warn("Unable to register Ender IO Tank storage compatibility", exception);
        }
    }
}
