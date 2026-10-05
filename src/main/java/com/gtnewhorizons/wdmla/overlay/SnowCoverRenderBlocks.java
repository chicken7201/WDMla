package com.gtnewhorizons.wdmla.overlay;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

/** Renders snow-covered surfaces in a single-block preview regardless of the underlying block type. */
final class SnowCoverRenderBlocks extends RenderBlocks {

    /** Uses the actual snow cover's texture and tint for a top face that touches it. */
    @Override
    public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
        boolean savedAo = enableAO;
        try {
            if (canRenderSnowCover()) {
                int blockX = MathHelper.floor_double(x);
                int blockY = MathHelper.floor_double(y);
                int blockZ = MathHelper.floor_double(z);
                if (x == blockX && y == blockY && z == blockZ) {
                    Block cover = blockAccess.getBlock(blockX, blockY + 1, blockZ);
                    Material material = cover.getMaterial();
                    if (material == Material.snow || material == Material.craftedSnow) {
                        icon = getBlockIcon(cover, blockAccess, blockX, blockY + 1, blockZ, 1);
                        Tessellator.instance
                                .setColorOpaque_I(cover.colorMultiplier(blockAccess, blockX, blockY + 1, blockZ));
                        // The preview disables AO; custom renderers must not reapply the base block's corner tint.
                        enableAO = false;
                    }
                }
            }
            super.renderFaceYPos(block, x, y, z, icon);
        } finally {
            enableAO = savedAo;
        }
    }

    /** Restricts snow replacement to a complete upper surface and preserves explicit texture overrides. */
    private boolean canRenderSnowCover() {
        return blockAccess != null && !hasOverrideBlockTexture()
                && renderMinX == 0
                && renderMaxX == 1
                && renderMaxY == 1
                && renderMinZ == 0
                && renderMaxZ == 1;
    }
}
