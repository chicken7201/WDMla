package com.gtnewhorizons.wdmla.plugin.railcraft;

import java.text.DecimalFormat;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.config.PluginsConfig;
import com.gtnewhorizons.wdmla.impl.ui.StatusHelper;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

import mods.railcraft.api.electricity.IElectricGrid;
import mods.railcraft.api.tracks.ITrackInstance;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.alpha.TileTankWater;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFirebox;
import mods.railcraft.common.blocks.machine.beta.TileBoilerTank;
import mods.railcraft.common.blocks.machine.beta.TileEngine;
import mods.railcraft.common.blocks.tracks.TileTrack;
import mods.railcraft.common.blocks.tracks.TrackElectric;
import mods.railcraft.common.items.ItemElectricMeter;
import mods.railcraft.common.plugins.buildcraft.triggers.ITemperature;

/** Native WDMla rendering of all Railcraft details supplied by WAILAPlugins. */
public enum RailcraftDetailsProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final DecimalFormat CHARGE_FORMAT = new DecimalFormat("#.##");

    /** Displays multiblock state, heat, engine output, charge, and water collection rate. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile == null) {
            return;
        }
        NBTTagCompound data = accessor.getServerData();
        if (tile instanceof TileMultiBlock) {
            tooltip.child(
                    data.getBoolean("RailcraftFormed") ? StatusHelper.INSTANCE.runningFine()
                            : StatusHelper.INSTANCE.structureIncomplete());
        }
        if (data.hasKey("RailcraftHeat")) {
            appendHeat(tooltip, data);
        }
        if (tile instanceof TileEngine) {
            tooltip.progress(
                    data.getInteger("RailcraftEnergy"),
                    data.getInteger("RailcraftMaxEnergy"),
                    "RF");
            tooltip.child(
                    value(
                            "hud.msg.wdmla.railcraft.generating",
                            Math.round(data.getFloat("RailcraftOutput")) + " RF/t"));
        }
        if (data.hasKey("RailcraftCharge")) {
            appendCharge(tooltip, data, accessor.getPlayer());
        }
        if (tile instanceof TileTankWater && data.getBoolean("RailcraftFormed")) {
            appendWaterTankRate(tooltip, data);
        }
    }

    /** Displays a temperature value against its maximum when known. */
    static void appendHeat(ITooltip tooltip, NBTTagCompound data) {
        int heat = Math.round(data.getFloat("RailcraftHeat"));
        int maximum = Math.round(data.getFloat("RailcraftMaxHeat"));
        if (maximum > 0) {
            tooltip.progress(heat, maximum, StatCollector.translateToLocal("hud.msg.wdmla.railcraft.temperature"));
        } else {
            tooltip.child(value("hud.msg.wdmla.railcraft.temperature", String.valueOf(heat)));
        }
    }

    /** Preserves WAILAPlugins' electric-meter-in-hand requirement for charge values. */
    static void appendCharge(ITooltip tooltip, NBTTagCompound data, EntityPlayer player) {
        ItemStack held = player.getCurrentEquippedItem();
        boolean hasMeter = !PluginsConfig.wailaPlugins.railcraft.meterInHand
                || (held != null && held.getItem() == ItemElectricMeter.getItem().getItem());
        String charge = hasMeter ? CHARGE_FORMAT.format(data.getDouble("RailcraftCharge")) + "c"
                : StatCollector.translateToLocal("hud.msg.wdmla.railcraft.need.meter");
        tooltip.child(value("hud.msg.wdmla.railcraft.charge", charge));
    }

    /** Displays each factor contributing to Railcraft's water-tank collection rate. */
    private static void appendWaterTankRate(ITooltip tooltip, NBTTagCompound data) {
        float rate = data.getFloat("RailcraftWaterRate");
        tooltip.child(
                value(
                        "hud.msg.wdmla.railcraft.water.rate",
                        String.format("%.2f/t (%.2f L/s)", rate, WaterRate.convertToLitersPerSecond(rate))));
        tooltip.child(
                value(
                        "hud.msg.wdmla.railcraft.humidity",
                        String.format("x%.2f", data.getFloat("RailcraftHumidity"))));
        if (data.getFloat("RailcraftWeather") != 1F) {
            tooltip.child(
                    value(
                            "hud.msg.wdmla.railcraft.weather",
                            String.format("x%.2f", data.getFloat("RailcraftWeather"))));
        }
        if (data.getFloat("RailcraftInside") != 1F) {
            tooltip.child(
                    value(
                            "hud.msg.wdmla.railcraft.sky",
                            String.format("x%.2f", data.getFloat("RailcraftInside"))));
        }
    }

    /** Collects Railcraft block state from the multiblock master when applicable. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile instanceof TileMultiBlock multiBlock) {
            data.setBoolean("RailcraftFormed", multiBlock.isStructureValid());
            if (multiBlock.getMasterBlock() != null) {
                tile = multiBlock.getMasterBlock();
            }
        }
        if (tile instanceof ITemperature temperature) {
            data.setFloat("RailcraftHeat", temperature.getTemperature());
        }
        if (tile instanceof TileEngine) {
            appendEngineData(data, tile);
        }
        if (tile instanceof IElectricGrid electricGrid) {
            data.setDouble("RailcraftCharge", electricGrid.getChargeHandler().getCharge());
        }
        if (tile instanceof TileTrack trackTile) {
            ITrackInstance track = trackTile.getTrackInstance();
            if (track instanceof TrackElectric electricTrack) {
                data.setDouble("RailcraftCharge", electricTrack.getChargeHandler().getCharge());
            }
        }
        if (tile instanceof TileTankWater waterTank && waterTank.isStructureValid()) {
            WaterRate rate = new WaterRate(waterTank).calculate();
            data.setFloat("RailcraftWaterRate", rate.rate);
            data.setFloat("RailcraftHumidity", rate.humidity);
            data.setFloat("RailcraftInside", rate.inside);
            data.setFloat("RailcraftWeather", rate.weather);
        }
    }

    /** Reads Railcraft engine state without linking its optional CoFH energy super-interface. */
    private static void appendEngineData(NBTTagCompound data, Object engine) {
        try {
            float output = ((Number) engine.getClass().getField("currentOutput").get(engine)).floatValue();
            int energy = ((Number) engine.getClass().getMethod("getEnergy").invoke(engine)).intValue();
            int maximum = ((Number) engine.getClass().getMethod("maxEnergy").invoke(engine)).intValue();
            data.setFloat("RailcraftOutput", output);
            data.setInteger("RailcraftEnergy", energy);
            data.setInteger("RailcraftMaxEnergy", maximum);
            if (engine.getClass().getName().endsWith("TileEngineSteamHobby")) {
                Object boiler = engine.getClass().getField("boiler").get(engine);
                float maxHeat = ((Number) boiler.getClass().getMethod("getMaxHeat").invoke(boiler)).floatValue();
                data.setFloat("RailcraftMaxHeat", maxHeat);
            }
        } catch (ReflectiveOperationException e) {
            mcp.mobius.waila.Waila.log.warn("Unable to read Railcraft engine state", e);
        }
    }

    /** Builds a themed localized key/value row. */
    private static com.gtnewhorizons.wdmla.api.ui.IComponent value(String key, String value) {
        return ThemeHelper.INSTANCE.value(StatCollector.translateToLocal(key), value);
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("railcraft", "wailaplugins_details");
    }

    /** Calculates the same biome, roof, and weather multipliers used by WAILAPlugins. */
    private static final class WaterRate {

        private static final float RATE_TICK_RATIO = 0.125F;
        private final World world;
        private final int x;
        private final int y;
        private final int z;
        private float rate;
        private float humidity;
        private float inside;
        private float weather;

        /** Captures the multiblock master's coordinates. */
        private WaterRate(TileMultiBlock multiBlock) {
            world = multiBlock.getWorld();
            x = multiBlock.getMasterBlock().xCoord;
            y = multiBlock.getMasterBlock().yCoord;
            z = multiBlock.getMasterBlock().zCoord;
        }

        /** Computes every collection multiplier and the final rate. */
        private WaterRate calculate() {
            humidity = 10F * world.getBiomeGenForCoords(x, z).rainfall;
            inside = canSeeSky() ? 1F : 0.5F;
            weather = !world.isRaining() || inside < 1F ? 1F
                    : world.getBiomeGenForCoords(x, z).getEnableSnow() ? 0.5F : 3F;
            rate = Math.max(MathHelper.floor_float(humidity * inside * weather), 1F);
            return this;
        }

        /** Checks the water tank's full 3x3 roof area for an open sky column. */
        private boolean canSeeSky() {
            return IntStream.rangeClosed(x - 1, x + 1)
                    .anyMatch(sampleX -> IntStream.rangeClosed(z - 1, z + 1)
                            .anyMatch(sampleZ -> world.canBlockSeeTheSky(sampleX, y + 3, sampleZ)));
        }

        /** Converts Railcraft's internal collection rate into liters per second. */
        private static float convertToLitersPerSecond(float rate) {
            return rate * RATE_TICK_RATIO * 20F;
        }
    }
}
