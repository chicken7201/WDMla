package com.gtnewhorizons.wdmla.plugin.ic2;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

import ic2.api.crops.CropCard;
import ic2.api.crops.ICropTile;
import ic2.core.crop.TileEntityCrop;

/** Modern rendering of every IC2 crop detail supplied by WAILAPlugins. */
public enum IC2CropProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    /** Displays scan-gated crop identity, growth, stats, storage, and environment information. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        if (!(accessor.getTileEntity() instanceof ICropTile cropTile)) {
            return;
        }
        CropCard crop = cropTile.getCrop();
        if (crop == null) {
            return;
        }
        NBTTagCompound data = accessor.getServerData();
        int scanLevel = data.getByte("scanLevel");
        if (scanLevel > 0) {
            appendIdentityAndGrowth(tooltip, cropTile, crop, data);
        } else if (crop.tier() == 0) {
            tooltip.child(ThemeHelper.INSTANCE.danger(StatCollector.translateToLocal("hud.msg.wdmla.ic2.weed")));
        }
        if (scanLevel >= 4) {
            appendDetailedStats(tooltip, data);
        }
    }

    /** Displays crop name, tier, growth capability, optimal harvest state, size, and growth gauge. */
    private static void appendIdentityAndGrowth(
            ITooltip tooltip, ICropTile cropTile, CropCard crop, NBTTagCompound data) {
        tooltip.child(
                ThemeHelper.INSTANCE.value(
                        StatCollector.translateToLocal("hud.msg.wdmla.ic2.crop"),
                        crop.name() + " (" + StatCollector.translateToLocal("hud.msg.wdmla.tier") + " " + crop.tier()
                                + ")"));

        boolean canGrow = data.getBoolean("canGrow");
        boolean canHarvest = data.getBoolean("canHarvest");
        tooltip.child(
                (canGrow ? ThemeHelper.INSTANCE.success(StatCollector.translateToLocal("hud.msg.wdmla.can.grow"))
                        : ThemeHelper.INSTANCE.failure(StatCollector.translateToLocal("hud.msg.wdmla.cannot.grow"))));
        if (canHarvest) {
            tooltip.child(
                    ThemeHelper.INSTANCE.success(
                            StatCollector.translateToLocal("hud.msg.wdmla.ic2.can.harvest")));
        }

        int maximumSize = crop.maxSize();
        tooltip.child(
                ThemeHelper.INSTANCE.value(
                        StatCollector.translateToLocal("hud.msg.wdmla.ic2.size"),
                        cropTile.getSize() + " / " + maximumSize));
        int duration = crop.growthDuration(cropTile);
        int points = data.getInteger("growthPoints");
        if (cropTile.getSize() < maximumSize && duration > 0 && points >= 0) {
            tooltip.progress(points, duration, StatCollector.translateToLocal("hud.msg.wdmla.growth"));
        }
    }

    /** Displays fully scanned crop genetics, internal supplies, and environmental quality. */
    private static void appendDetailedStats(ITooltip tooltip, NBTTagCompound data) {
        tooltip.child(
                ThemeHelper.INSTANCE.value(
                        StatCollector.translateToLocal("hud.msg.wdmla.ic2.stats"),
                        String.format(
                                "G %d | Ga %d | R %d",
                                data.getByte("growth"),
                                data.getByte("gain"),
                                data.getByte("resistance"))));
        tooltip.child(
                ThemeHelper.INSTANCE.value(
                        StatCollector.translateToLocal("hud.msg.wdmla.ic2.supplies"),
                        String.format(
                                "F %d | W %d | Weed-Ex %d",
                                data.getInteger("fertilizer"),
                                data.getInteger("water"),
                                data.getInteger("weedex"))));
        tooltip.child(
                ThemeHelper.INSTANCE.value(
                        StatCollector.translateToLocal("hud.msg.wdmla.ic2.environment"),
                        String.format(
                                "N %d | H %d | A %d",
                                data.getInteger("nutrients"),
                                data.getInteger("humidity"),
                                data.getInteger("airQuality"))));
    }

    /** Collects all IC2 crop values used by the client renderer. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        if (!(accessor.getTileEntity() instanceof ICropTile cropTile)) {
            return;
        }
        CropCard crop = cropTile.getCrop();
        data.setBoolean("canGrow", crop != null && crop.canGrow(cropTile));
        data.setBoolean("canHarvest", crop != null && crop.canBeHarvested(cropTile));
        data.setInteger("optimalHarvest", crop != null ? crop.getOptimalHavestSize(cropTile) : -1);
        data.setByte("scanLevel", cropTile.getScanLevel());
        data.setByte("growth", cropTile.getGrowth());
        data.setByte("gain", cropTile.getGain());
        data.setByte("resistance", cropTile.getResistance());
        data.setInteger("fertilizer", cropTile.getNutrientStorage());
        data.setInteger("water", cropTile.getHydrationStorage());
        data.setInteger("weedex", cropTile.getWeedExStorage());
        data.setInteger("nutrients", cropTile.getNutrients());
        data.setInteger("humidity", cropTile.getHumidity());
        data.setInteger("airQuality", cropTile.getAirQuality());
        TileEntity tile = accessor.getTileEntity();
        data.setInteger("growthPoints", tile instanceof TileEntityCrop cropEntity ? cropEntity.growthPoints : -1);
    }

    /** Requests data only when the target tile implements IC2's crop interface. */
    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getTileEntity() instanceof ICropTile;
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("ic2", "crop_details");
    }
}
