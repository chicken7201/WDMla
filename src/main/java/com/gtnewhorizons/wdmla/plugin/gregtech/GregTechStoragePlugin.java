package com.gtnewhorizons.wdmla.plugin.gregtech;

import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.plugin.universal.FluidStorageProvider;
import com.gtnewhorizons.wdmla.plugin.universal.ItemStorageProvider;
import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Registers GregTech machine and pipe contents while retaining specialized digital storage capacities. */
@WDMlaPlugin(uid = "gregtech_storage", dependencies = "gregtech")
public class GregTechStoragePlugin implements IWDMlaPlugin {

    private static final String BASE_META_TILE_ENTITY = "gregtech.api.metatileentity.BaseMetaTileEntity";
    private static final String BASE_META_PIPE_ENTITY = "gregtech.api.metatileentity.BaseMetaPipeEntity";

    /** Registers specialized digital stores before generic machine, hatch, bus, and pipe storage adapters. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        try {
            Class<?> baseMetaTileEntity = Class.forName(BASE_META_TILE_ENTITY);
            Class<?> baseMetaPipeEntity = Class.forName(BASE_META_PIPE_ENTITY);
            GregTechItemStorageProvider itemCapacityProvider =
                    GregTechItemStorageProvider.create(baseMetaTileEntity);
            GregTechDigitalTankStorageProvider fluidCapacityProvider =
                    GregTechDigitalTankStorageProvider.create(baseMetaTileEntity);
            if (itemCapacityProvider != null) {
                registration.registerItemStorage(itemCapacityProvider, baseMetaTileEntity);
            }
            if (fluidCapacityProvider != null) {
                registration.registerFluidStorage(fluidCapacityProvider, baseMetaTileEntity);
            }
            registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, baseMetaTileEntity);
            registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, baseMetaTileEntity);
            registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, baseMetaPipeEntity);
            registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, baseMetaPipeEntity);
        } catch (ClassNotFoundException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
        }
    }
}
