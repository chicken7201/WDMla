package com.gtnewhorizons.wdmla.plugin.newintegrations;

import net.minecraft.util.ResourceLocation;

/** Shows the compressor's collected ingredients against its recipe cost. */
public final class AvaritiaCompressorProvider extends OptionalProgressProvider {

    static final AvaritiaCompressorProvider INSTANCE = new AvaritiaCompressorProvider();
    static final String TILE_CLASS = "fox.spiteful.avaritia.tile.TileEntityCompressor";

    /** Binds this provider to the Avaritia compressor name. */
    private AvaritiaCompressorProvider() {
        super(TILE_CLASS, "hud.msg.wdmla.avaritia.ingredients");
    }

    /** Returns the independent configuration and synchronization identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("avaritia", "compressor_progress");
    }

    /** Names this option using the project's dotted translation convention. */
    @Override
    public String getLangKey() {
        return "provider.wdmla.avaritia.compressor_progress";
    }
}
