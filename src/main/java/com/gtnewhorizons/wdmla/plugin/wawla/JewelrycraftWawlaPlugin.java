package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mcp.mobius.waila.Waila;

/** Registers the Jewelrycraft tile details from WAWLA when that optional mod is loaded. */
@WDMlaPlugin(uid = "wawla_jewelrycraft", dependencies = "Jewelrycraft")
public class JewelrycraftWawlaPlugin implements IWDMlaPlugin {

    private static final String[] TILES = {
            "darkknight.jewelrycraft.tileentity.TileEntitySmelter",
            "darkknight.jewelrycraft.tileentity.TileEntityMolder" };

    /** Registers the tile-sensitive Jewelrycraft client renderer. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(JewelrycraftWawlaProvider.INSTANCE, Block.class);
    }

    /** Registers the two Jewelrycraft tile serializers used by WAWLA. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        for (String tile : TILES) {
            try {
                registration.registerBlockDataProvider(JewelrycraftWawlaProvider.INSTANCE, Class.forName(tile));
            } catch (ClassNotFoundException e) {
                Waila.log.warn("Jewelrycraft is loaded but WAWLA target {} is missing", tile);
            }
        }
    }
}
