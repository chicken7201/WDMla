package com.gtnewhorizons.wdmla.plugin.core;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

import mcp.mobius.waila.api.SpecialChars;

/** Reimplements WAWLA's block light and hostile-spawn information. */
public enum LightLevelProvider implements IBlockComponentProvider {

    INSTANCE;

    /** Displays block light at night and the daylight-adjusted value above the targeted block. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        int x = accessor.getHitResult().blockX;
        int y = accessor.getHitResult().blockY;
        int z = accessor.getHitResult().blockZ;
        World world = accessor.getWorld();
        if (world.isBlockNormalCubeDefault(x, y + 1, z, false) && !world.isAirBlock(x, y + 1, z)) {
            return;
        }

        int nightLevel = getBlockLightLevel(world, x, y, z, false);
        int dayLevel = getBlockLightLevel(world, x, y, z, true);
        String spawnColor = nightLevel <= 7 ? SpecialChars.RED : SpecialChars.GREEN;
        String value = spawnColor + nightLevel + SpecialChars.RESET + SpecialChars.YELLOW + " (" + dayLevel + ")";
        tooltip.child(
                ThemeHelper.INSTANCE.value(StatCollector.translateToLocal("hud.msg.wdmla.light.level"), value));
    }

    /** Reads the exact light sample used by WAWLA, one block above the target. */
    private static int getBlockLightLevel(World world, int x, int y, int z, boolean daylight) {
        int skylightSubtracted = daylight ? 0 : 16;
        return world.getChunkFromChunkCoords(x >> 4, z >> 4)
                .getBlockLightValue(x & 0xF, y + 1, z & 0xF, skylightSubtracted);
    }

    /** Returns the stable configuration identifier for the light-level provider. */
    @Override
    public ResourceLocation getUid() {
        return Identifiers.LIGHT_LEVEL;
    }
}
