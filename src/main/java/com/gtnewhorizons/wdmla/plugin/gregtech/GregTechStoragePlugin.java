package com.gtnewhorizons.wdmla.plugin.gregtech;

import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.plugin.universal.FluidStorageProvider;

import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Registers native WDMla storage views for GregTech machines and hatches. */
@WDMlaPlugin(uid = "gregtech_storage", dependencies = "gregtech")
public class GregTechStoragePlugin implements IWDMlaPlugin {

    private static final String BASE_META_TILE_ENTITY = "gregtech.api.metatileentity.BaseMetaTileEntity";

    /** Registers GregTech item inventories and fluid tanks on the common side. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        try {
            Class<?> baseMetaTileEntity = Class.forName(BASE_META_TILE_ENTITY);
            registration.registerItemStorage(GregTechItemStorageProvider.INSTANCE, baseMetaTileEntity);
            registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, baseMetaTileEntity);
        } catch (ClassNotFoundException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
        }
    }
}
