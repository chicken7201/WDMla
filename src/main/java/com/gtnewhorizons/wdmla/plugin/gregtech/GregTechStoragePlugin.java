package com.gtnewhorizons.wdmla.plugin.gregtech;

import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Registers native WDMla storage views only for dedicated GregTech storage machines. */
@WDMlaPlugin(uid = "gregtech_storage", dependencies = "gregtech")
public class GregTechStoragePlugin implements IWDMlaPlugin {

    private static final String BASE_META_TILE_ENTITY = "gregtech.api.metatileentity.BaseMetaTileEntity";

    /** Registers the wrapper adapter whose runtime predicate accepts only digital chest meta tiles. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        try {
            Class<?> baseMetaTileEntity = Class.forName(BASE_META_TILE_ENTITY);
            GregTechItemStorageProvider provider = GregTechItemStorageProvider.create(baseMetaTileEntity);
            if (provider != null) {
                registration.registerItemStorage(provider, baseMetaTileEntity);
            }
        } catch (ClassNotFoundException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
        }
    }
}
