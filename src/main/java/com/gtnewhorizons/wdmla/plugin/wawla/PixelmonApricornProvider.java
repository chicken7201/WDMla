package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

/** Displays Pixelmon apricorn growth and product for either half of the two-block tree. */
public enum PixelmonApricornProvider implements IBlockComponentProvider {

    INSTANCE;

    private static final String APRICORN_TILE =
            "com.pixelmonmod.pixelmon.blocks.apricornTrees.TileEntityApricornTree";

    /** Resolves the lower tile, then displays a growth gauge and safe registry-derived product name. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        int x = accessor.getHitResult().blockX;
        int y = accessor.getHitResult().blockY;
        int z = accessor.getHitResult().blockZ;
        TileEntity tile = accessor.getTileEntity();
        if (tile == null || !APRICORN_TILE.equals(tile.getClass().getName())) {
            tile = accessor.getWorld().getTileEntity(x, y - 1, z);
        }
        if (tile == null || !APRICORN_TILE.equals(tile.getClass().getName())) {
            return;
        }
        int growth = tile.getWorldObj().getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord);
        tooltip.progress(
                Math.min(growth, 5),
                5,
                StatCollector.translateToLocal("hud.msg.wdmla.pixelmon.growth"));

        String registryName = Block.blockRegistry.getNameForObject(accessor.getBlock());
        String product = registryName == null ? "?" : registryName.substring(registryName.indexOf(':') + 1)
                .replace("ApricornTree", "").replace("apricornTree", "");
        tooltip.child(
                ThemeHelper.INSTANCE.value(
                        StatCollector.translateToLocal("hud.msg.wdmla.pixelmon.product"),
                        product));
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("pixelmon", "wawla_apricorn");
    }
}
