package com.gtnewhorizons.wdmla.plugin.newintegrations;

import net.minecraft.util.ResourceLocation;

/** Shows Witching Gadgets' current spinning operation. */
public final class SpinningWheelProvider extends OptionalProgressProvider {

    static final SpinningWheelProvider INSTANCE = new SpinningWheelProvider();
    static final String TILE_CLASS = "witchinggadgets.common.blocks.tiles.TileEntitySpinningWheel";

    /** Binds this provider to the spinning wheel tile name. */
    private SpinningWheelProvider() {
        super(TILE_CLASS, "hud.msg.wdmla.progress");
    }

    /** Returns the independent configuration and synchronization identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("witchinggadgets", "spinning_wheel_progress");
    }

    /** Names this option using the project's dotted translation convention. */
    @Override
    public String getLangKey() {
        return "provider.wdmla.witchinggadgets.spinning_wheel_progress";
    }
}
