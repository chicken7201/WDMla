package com.gtnewhorizons.wdmla.impl.ui.drawable;

import com.gtnewhorizons.wdmla.api.ui.IDrawable;
import com.gtnewhorizons.wdmla.api.ui.sizer.IArea;
import com.gtnewhorizons.wdmla.config.PluginsConfig;
import com.gtnewhorizons.wdmla.overlay.GuiBlockDraw;

import mcp.mobius.waila.overlay.OverlayConfig;

public class BlockDrawable implements IDrawable {

    private static final float SIZE_MULTIPLIER = 1.5f;

    protected static float rotationPitch = 30f;

    private final int blockX;
    private final int blockY;
    private final int blockZ;

    /** Stores the world coordinates rendered by this block model component. */
    public BlockDrawable(int blockX, int blockY, int blockZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    /** Advances the shared block-model rotation for one actively targeted client tick. */
    public static void advanceRotation() {
        rotationPitch += PluginsConfig.core.defaultBlock.rendererRotationSpeed;
    }

    /** Renders the selected world block using the rotation accumulated only while targeting blocks. */
    @Override
    public void draw(IArea area) {
        // custom viewport is unaffected by GLScalef
        GuiBlockDraw.drawWorldBlock(
                (int) ((area.getX() - area.getW() * (SIZE_MULTIPLIER - 1) / 2) * OverlayConfig.scale),
                (int) ((area.getY() - area.getH() * (SIZE_MULTIPLIER - 1) / 2) * OverlayConfig.scale),
                (int) (area.getW() * OverlayConfig.scale * SIZE_MULTIPLIER),
                (int) (area.getH() * OverlayConfig.scale * SIZE_MULTIPLIER),
                blockX,
                blockY,
                blockZ,
                30f,
                rotationPitch);
    }
}
