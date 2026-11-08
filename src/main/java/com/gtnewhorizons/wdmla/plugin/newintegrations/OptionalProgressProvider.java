package com.gtnewhorizons.wdmla.plugin.newintegrations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;

import mcp.mobius.waila.Waila;

/** Shared server synchronization for two independently toggleable progress providers. */
abstract class OptionalProgressProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private static final String CURRENT = "WDMlaOptionalProgressCurrent";
    private static final String MAXIMUM = "WDMlaOptionalProgressMaximum";
    private final String tileName;
    private final String labelKey;
    private Method currentMethod;
    private Method maximumMethod;
    private Field currentField;
    private Field maximumField;

    /** Stores the exact target class name without linking to an absent mod. */
    OptionalProgressProvider(String tileName, String labelKey) {
        this.tileName = tileName;
        this.labelKey = labelKey;
    }

    /** Resolves and caches the verified API members during registration. */
    final void bind(Class<?> tileClass) throws NoSuchMethodException, NoSuchFieldException {
        if (this instanceof AvaritiaCompressorProvider) {
            currentMethod = tileClass.getMethod("getProgress");
            maximumMethod = tileClass.getMethod("getTarget");
        } else {
            currentField = tileClass.getField("progress");
            maximumField = tileClass.getField("maxProgress");
        }
    }

    /** Displays the server supplied count only for this exact optional tile. */
    @Override
    public final void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound data = accessor.getServerData();
        if (tile == null || !tileName.equals(tile.getClass().getName()) || !data.hasKey(maximumKey())) {
            return;
        }
        int maximum = data.getInteger(maximumKey());
        if (maximum > 0) {
            tooltip.progress(data.getInteger(currentKey()), maximum, StatCollector.translateToLocal(labelKey));
        }
    }

    /** Sends two bounded integers and never serializes the whole inventory. */
    @Override
    public final void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile == null || tile.isInvalid() || !tileName.equals(tile.getClass().getName())) {
            return;
        }
        try {
            int maximum = read(maximumMethod, maximumField, tile);
            int current = read(currentMethod, currentField, tile);
            if (this instanceof SpinningWheelProvider && current == 0) {
                return;
            }
            if (maximum > 0 && current >= 0) {
                data.setInteger(currentKey(), Math.min(current, maximum));
                data.setInteger(maximumKey(), maximum);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Waila.log.warn("Unable to read optional progress tile: " + tileName, e);
        }
    }

    /** Reads a cached public getter or field from the tile. */
    private static int read(Method method, Field field, TileEntity tile)
            throws IllegalAccessException, InvocationTargetException {
        return method != null ? ((Number) method.invoke(tile)).intValue() : field.getInt(tile);
    }

    /** Gives each provider an independent current-value NBT key. */
    private String currentKey() {
        return tileName + CURRENT;
    }

    /** Gives each provider an independent maximum-value NBT key. */
    private String maximumKey() {
        return tileName + MAXIMUM;
    }
}
