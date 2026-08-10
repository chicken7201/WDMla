package com.gtnewhorizons.wdmla.plugin.enderstorage;

import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.plugin.universal.FluidStorageProvider;
import com.gtnewhorizons.wdmla.plugin.universal.ItemStorageProvider;

import mcp.mobius.waila.Waila;

/** Adds content views for EnderStorage tiles while preserving its existing channel-color provider. */
@WDMlaPlugin(uid = "enderstorage_storage", dependencies = "EnderStorage")
public class EnderStoragePlugin implements IWDMlaPlugin {

    private static final String ENDER_CHEST = "codechicken.enderstorage.storage.item.TileEnderChest";
    private static final String ENDER_TANK = "codechicken.enderstorage.storage.liquid.TileEnderTank";

    /** Registers exact chest and tank tile classes backed by EnderStorage's shared server storage objects. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registerItemStorage(registration, ENDER_CHEST);
        registerFluidStorage(registration, ENDER_TANK);
    }

    /** Registers the Ender Chest's delegated shared inventory with the standard item preview. */
    private static void registerItemStorage(IWDMlaCommonRegistration registration, String className) {
        try {
            registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, Class.forName(className));
        } catch (ClassNotFoundException exception) {
            Waila.log.warn("Unable to register EnderStorage item storage compatibility for {}", className, exception);
        }
    }

    /** Registers the Ender Tank's delegated shared tank with the standard synchronized fluid view. */
    private static void registerFluidStorage(IWDMlaCommonRegistration registration, String className) {
        try {
            registration.registerFluidStorage(FluidStorageProvider.Extension.INSTANCE, Class.forName(className));
        } catch (ClassNotFoundException exception) {
            Waila.log.warn("Unable to register EnderStorage fluid storage compatibility for {}", className, exception);
        }
    }
}
