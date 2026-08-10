package com.gtnewhorizons.wdmla.plugin.extrautilities;

import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.plugin.universal.ItemStorageProvider;

import mcp.mobius.waila.Waila;

/** Adds an item preview for Extra Utilities' dedicated Filing Cabinet inventory. */
@WDMlaPlugin(uid = "extra_utilities_storage", dependencies = "ExtraUtilities")
public class ExtraUtilitiesStoragePlugin implements IWDMlaPlugin {

    private static final String FILING_CABINET = "com.rwtema.extrautils.tileentity.TileEntityFilingCabinet";

    /** Registers only the Filing Cabinet TileEntity, leaving other Extra Utilities inventories untouched. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        try {
            registration.registerItemStorage(ItemStorageProvider.Extension.INSTANCE, Class.forName(FILING_CABINET));
        } catch (ClassNotFoundException exception) {
            Waila.log.warn("Unable to register Extra Utilities Filing Cabinet storage compatibility", exception);
        }
    }
}
