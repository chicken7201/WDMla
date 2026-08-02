package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

/** Native WDMla rendering of all Thaumcraft details that WAWLA exposed. */
public enum ThaumcraftWawlaProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    /** Selects and renders the details belonging to the current Thaumcraft tile. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile == null) {
            return;
        }
        String type = tile.getClass().getName();
        NBTTagCompound data = accessor.getServerData();
        if (type.endsWith("TileJarFillable") || type.endsWith("TileJarFillableVoid")) {
            appendEssentiaJar(tooltip, data);
        } else if (type.endsWith("TileMirror") || type.endsWith("TileMirrorEssentia")) {
            appendMirror(tooltip, data);
        } else if (type.endsWith("TileJarBrain")) {
            tooltip.child(value("hud.msg.wdmla.experience", String.valueOf(data.getInteger("XP"))));
        } else if (type.endsWith("TileDeconstructionTable")) {
            appendAspect(tooltip, data.getString("Aspect"));
        } else if (type.endsWith("TileWandPedestal")) {
            appendWandPedestal(tooltip, data);
        } else if (type.endsWith("TilePedestal")) {
            ItemStack stack = getStackInSlot(data, 0);
            if (stack != null) {
                tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(stack));
            }
        }
    }

    /** Displays the jar's aspect name and stored essentia amount. */
    private static void appendEssentiaJar(ITooltip tooltip, NBTTagCompound data) {
        appendAspect(tooltip, data.getString("Aspect"));
        int amount = data.getShort("Amount");
        if (amount > 0) {
            tooltip.child(value("hud.msg.wdmla.amount", String.valueOf(amount)));
        }
    }

    /** Displays mirror destination coordinates and dimension when linked. */
    private static void appendMirror(ITooltip tooltip, NBTTagCompound data) {
        if (!data.getBoolean("linked")) {
            return;
        }
        String position = String.format(
                "X: %d, Y: %d, Z: %d",
                data.getInteger("linkX"),
                data.getInteger("linkY"),
                data.getInteger("linkZ"));
        tooltip.child(value("hud.msg.wdmla.thaumcraft.linked", position));

        WorldProvider provider = DimensionManager.getProvider(data.getInteger("linkDim"));
        if (provider != null) {
            tooltip.child(value("hud.msg.wdmla.dimension", provider.getDimensionName()));
        }
    }

    /** Displays the pedestal wand and its six primal-aspect charge values. */
    private static void appendWandPedestal(ITooltip tooltip, NBTTagCompound data) {
        ItemStack wand = getStackInSlot(data, 0);
        if (wand == null) {
            return;
        }
        tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(wand));
        NBTTagCompound wandData = wand.getTagCompound();
        if (wandData == null || !wandData.hasKey("aqua")) {
            return;
        }
        String charge = String.format(
                "Aer %.2f | Terra %.2f | Ignis %.2f | Aqua %.2f | Ordo %.2f | Perditio %.2f",
                wandData.getInteger("aer") / 100F,
                wandData.getInteger("terra") / 100F,
                wandData.getInteger("ignis") / 100F,
                wandData.getInteger("aqua") / 100F,
                wandData.getInteger("ordo") / 100F,
                wandData.getInteger("perditio") / 100F);
        tooltip.child(value("hud.msg.wdmla.thaumcraft.wand.charge", charge));
    }

    /** Adds an aspect row only when the tile actually contains an aspect. */
    private static void appendAspect(ITooltip tooltip, String aspect) {
        if (aspect != null && !aspect.isEmpty()) {
            String displayName = Character.toUpperCase(aspect.charAt(0)) + aspect.substring(1);
            tooltip.child(value("hud.msg.wdmla.thaumcraft.aspect", displayName));
        }
    }

    /** Builds a themed localized key/value row. */
    private static com.gtnewhorizons.wdmla.api.ui.IComponent value(String key, String value) {
        return ThemeHelper.INSTANCE.value(StatCollector.translateToLocal(key), value);
    }

    /** Reads a serialized inventory slot from the standard Items tag. */
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

    /** Synchronizes the tile's serialized state exactly as WAWLA did. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile != null) {
            tile.writeToNBT(data);
        }
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("thaumcraft", "wawla_details");
    }
}
