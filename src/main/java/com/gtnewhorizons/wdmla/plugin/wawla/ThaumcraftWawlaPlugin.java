package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mcp.mobius.waila.Waila;

/** Registers the Thaumcraft portion of WAWLA when Thaumcraft is available. */
@WDMlaPlugin(uid = "wawla_thaumcraft", dependencies = "Thaumcraft")
public class ThaumcraftWawlaPlugin implements IWDMlaPlugin {

    private static final String[] TILE_CLASSES = {
            "thaumcraft.common.tiles.TileJarFillable",
            "thaumcraft.common.tiles.TileJarFillableVoid",
            "thaumcraft.common.tiles.TileMirror",
            "thaumcraft.common.tiles.TileMirrorEssentia",
            "thaumcraft.common.tiles.TileJarBrain",
            "thaumcraft.common.tiles.TileWandPedestal",
            "thaumcraft.common.tiles.TilePedestal",
            "thaumcraft.common.tiles.TileDeconstructionTable" };

    /** Registers one conditional client renderer that checks the actual Thaumcraft tile type. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(ThaumcraftWawlaProvider.INSTANCE, Block.class);
    }

    /** Registers server synchronization for every Thaumcraft tile supported by WAWLA. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        for (String className : TILE_CLASSES) {
            try {
                registration.registerBlockDataProvider(ThaumcraftWawlaProvider.INSTANCE, Class.forName(className));
            } catch (ClassNotFoundException e) {
                Waila.log.debug("WAWLA Thaumcraft target {} is not present", className);
            }
        }
    }
}
