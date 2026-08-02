package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

/** Native WDMla rendering of WAWLA's Jewelrycraft smelting and molding details. */
public enum JewelrycraftWawlaProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    /** Displays molten/unmelted metal and remaining melting or cooling time. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile == null) {
            return;
        }
        NBTTagCompound data = accessor.getServerData();
        String type = tile.getClass().getName();
        if (type.endsWith("TileEntitySmelter")) {
            appendSmelter(tooltip, data);
        } else if (type.endsWith("TileEntityMolder") && data.getInteger("cooling") > 0) {
            tooltip.child(
                    value(
                            "hud.msg.wdmla.jewelry.cooling",
                            data.getInteger("cooling") / 20 + "s"));
        }
    }

    /** Displays all WAWLA smelter rows. */
    private static void appendSmelter(ITooltip tooltip, NBTTagCompound data) {
        if (data.getBoolean("hasMoltenMetal")) {
            ItemStack metal = getMetalStack(data.getCompoundTag("moltenMetal"));
            if (metal != null) {
                tooltip.child(
                        value(
                                "hud.msg.wdmla.jewelry.molten",
                                metal.getDisplayName() + " x " + String.format("%.2f", data.getFloat("quantity") * 10)));
            }
        }
        if (data.getBoolean("hasMetal")) {
            ItemStack metal = getMetalStack(data.getCompoundTag("metal"));
            if (metal != null) {
                tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(metal));
            }
            tooltip.child(
                    value(
                            "hud.msg.wdmla.jewelry.melt.time",
                            data.getInteger("melting") / 20 + "s"));
        }
    }

    /** Reconstructs Jewelrycraft's compact metal item NBT. */
    private static ItemStack getMetalStack(NBTTagCompound metal) {
        Item item = Item.getItemById(metal.getShort("id"));
        return item == null ? null : new ItemStack(item, metal.getByte("Count"), metal.getShort("Damage"));
    }

    /** Synchronizes Jewelrycraft's tile NBT exactly as WAWLA did. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile != null) {
            tile.writeToNBT(data);
        }
    }

    /** Builds a themed localized key/value row. */
    private static com.gtnewhorizons.wdmla.api.ui.IComponent value(String key, String value) {
        return ThemeHelper.INSTANCE.value(StatCollector.translateToLocal(key), value);
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("jewelrycraft", "wawla_details");
    }
}
