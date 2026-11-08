package com.gtnewhorizons.wdmla.plugin.newintegrations;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mcp.mobius.waila.Waila;

/** Registers progress information only when the optional tile classes exist. */
@WDMlaPlugin(uid = "optional_progress")
public class OptionalProgressPlugin implements IWDMlaPlugin {

    /** Registers both client renderers; each ignores unrelated blocks. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerBlockComponent(AvaritiaCompressorProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(SpinningWheelProvider.INSTANCE, Block.class);
    }

    /** Resolves optional classes only after normal plugin scanning. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registerTile(registration, AvaritiaCompressorProvider.TILE_CLASS, AvaritiaCompressorProvider.INSTANCE);
        registerTile(registration, SpinningWheelProvider.TILE_CLASS, SpinningWheelProvider.INSTANCE);
    }

    /** Registers one tile when its owning mod is present. */
    private static void registerTile(IWDMlaCommonRegistration registration, String className,
            OptionalProgressProvider provider) {
        try {
            Class<?> tileClass = Class.forName(className);
            provider.bind(tileClass);
            registration.registerBlockDataProvider(provider, tileClass);
        } catch (ClassNotFoundException | LinkageError e) {
            Waila.log.debug("Optional progress tile unavailable: " + className, e);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            Waila.log.warn("Optional progress API changed: " + className, e);
        }
    }
}
