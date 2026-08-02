package com.gtnewhorizons.wdmla.plugin.tconstruct;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.IComponent;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

/** Reimplements WAWLA's drying-rack, slab-furnace, and hidden-landmine integration. */
public enum TConstructWawlaProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String DRYING_RACK = "tconstruct.blocks.logic.DryingRackLogic";
    private static final String SLAB_FURNACE = "tconstruct.tools.logic.FurnaceLogic";
    private static final String LANDMINE = "tconstruct.mechworks.blocks.BlockLandmine";

    /** Renders the appropriate modern component for the targeted TConstruct block. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile == null) {
            return;
        }

        String tileClass = tile.getClass().getName();
        NBTTagCompound data = accessor.getServerData();
        if (DRYING_RACK.equals(tileClass)) {
            appendDryingRack(tooltip, data);
        } else if (SLAB_FURNACE.equals(tileClass)) {
            appendSlabFurnace(tooltip, data, accessor.showDetails());
        } else if (LANDMINE.equals(accessor.getBlock().getClass().getName())) {
            ItemStack cover = getStackInSlot(data, 3);
            if (cover != null) {
                ThemeHelper.INSTANCE.overrideTooltipHeader(tooltip, cover);
            }
        }
    }

    /** Displays the drying item and WAWLA's Time/MaxTime value as a textured progress gauge. */
    private static void appendDryingRack(ITooltip tooltip, NBTTagCompound data) {
        ItemStack drying = getStackInSlot(data, 0);
        if (drying != null) {
            tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(drying));
        }
        int current = data.getInteger("Time");
        int maximum = data.getInteger("MaxTime");
        if (current > 0 && maximum > 0) {
            tooltip.progress(
                    Math.min(current, maximum),
                    maximum,
                    StatCollector.translateToLocal("hud.msg.wdmla.tconstruct.dryness"));
        }
    }

    /** Displays slab-furnace inventory and burn state using WDMla's furnace layout. */
    private static void appendSlabFurnace(ITooltip tooltip, NBTTagCompound data, boolean showDetails) {
        ItemStack input = getStackInSlot(data, 0);
        ItemStack output = getStackInSlot(data, 2);
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        inputs.add(input);
        outputs.add(output);

        int progress = firstPositive(data, "Progress", "CookTime", "BurnTime");
        int maximum = firstPositive(data, "MaxProgress", "MaxCookTime", "Fuel");
        if (input != null || output != null || (progress > 0 && maximum > 0)) {
            IComponent furnace = ThemeHelper.INSTANCE
                    .furnaceLikeProgress(inputs, outputs, progress, Math.max(maximum, 1), showDetails);
            if (furnace != null) {
                tooltip.child(furnace);
            }
        }
        ItemStack fuel = getStackInSlot(data, 1);
        if (showDetails && fuel != null) {
            tooltip.child(
                    ThemeHelper.INSTANCE.value(
                            StatCollector.translateToLocal("hud.msg.wdmla.fuel"),
                            fuel.stackSize + "x " + fuel.getDisplayName()));
        }
    }

    /** Returns the first positive integer stored under one of the supplied compatibility keys. */
    private static int firstPositive(NBTTagCompound data, String... keys) {
        for (String key : keys) {
            int value = data.getInteger(key);
            if (value > 0) {
                return value;
            }
        }
        return 0;
    }

    /** Reads an inventory slot from the standard TileEntity Items list used by WAWLA. */
    private static ItemStack getStackInSlot(NBTTagCompound data, int slot) {
        NBTTagList items = data.getTagList("Items", 10);
        for (int index = 0; index < items.tagCount(); index++) {
            NBTTagCompound item = items.getCompoundTagAt(index);
            if ((item.getByte("Slot") & 0xFF) == slot) {
                return ItemStack.loadItemStackFromNBT(item);
            }
        }
        return null;
    }

    /** Copies TConstruct's public serialized tile state for the client-side compatibility renderer. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile != null) {
            tile.writeToNBT(data);
        }
    }

    /** Requests synchronized data only for the three WAWLA-supported TConstruct targets. */
    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile == null) {
            return false;
        }
        String tileName = tile.getClass().getName();
        return DRYING_RACK.equals(tileName)
                || SLAB_FURNACE.equals(tileName)
                || LANDMINE.equals(accessor.getBlock().getClass().getName());
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("tconstruct", "wawla_details");
    }
}
