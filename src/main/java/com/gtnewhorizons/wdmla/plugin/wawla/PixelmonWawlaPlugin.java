package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mcp.mobius.waila.Waila;

/** Registers both Pixelmon integrations that are active in the official WAWLA entry point. */
@WDMlaPlugin(uid = "wawla_pixelmon", dependencies = "pixelmon")
public class PixelmonWawlaPlugin implements IWDMlaPlugin {

    private static final String ENTITY = "com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon";
    private static final String APRICORN_BLOCK =
            "com.pixelmonmod.pixelmon.blocks.apricornTrees.BlockApricornTree";

    /** Registers Pixelmon entity and apricorn client components against their exact optional classes. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        try {
            registration.registerEntityComponent(PixelmonEntityProvider.INSTANCE, loadEntityClass(ENTITY));
            registration.registerBlockComponent(PixelmonApricornProvider.INSTANCE, loadBlockClass(APRICORN_BLOCK));
        } catch (ClassNotFoundException e) {
            Waila.log.warn("Pixelmon is loaded but its WAWLA target classes were not found", e);
        }
    }

    /** Registers full Pixelmon entity synchronization exactly as WAWLA did. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        try {
            registration.registerEntityDataProvider(PixelmonEntityProvider.INSTANCE, loadEntityClass(ENTITY));
        } catch (ClassNotFoundException e) {
            Waila.log.warn("Pixelmon entity class was not found", e);
        }
    }

    /** Loads and validates an optional entity class. */
    private static Class<? extends Entity> loadEntityClass(String name) throws ClassNotFoundException {
        return Class.forName(name).asSubclass(Entity.class);
    }

    /** Loads and validates an optional block class. */
    private static Class<? extends Block> loadBlockClass(String name) throws ClassNotFoundException {
        return Class.forName(name).asSubclass(Block.class);
    }
}
