package mcp.mobius.waila.addons.thermalexpansion;

import java.text.NumberFormat;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.utils.WailaExceptionHandler;

public class HUDHandlerIEnergyHandler implements IWailaDataProvider {

    private static final NumberFormat energyFormat = NumberFormat.getInstance();
    
    static {
        energyFormat.setGroupingUsed(true);
        energyFormat.setMaximumFractionDigits(0);
    }

    /** Leaves the target stack unchanged for RF energy providers. */
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    /** Leaves the tooltip header unchanged because RF storage is body information. */
    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    /** Adds the configured modern RF bar or the legacy numeric RF text. */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

        if (!config.getConfig("thermalexpansion.energyhandler")) return currenttip;
        if (!accessor.getNBTData().hasKey("Energy")) return currenttip;

        if (!accessor.getNBTData().hasKey("MaxStorage")) return currenttip;
        try {
            if (currenttip instanceof ITaggedList) {
                ITaggedList<String, String> taggedTips = (ITaggedList<String, String>) currenttip;
                int energy = accessor.getNBTInteger(accessor.getNBTData(), "Energy");
                int maxEnergy = accessor.getNBTInteger(accessor.getNBTData(), "MaxStorage");
                if (maxEnergy > 0 && taggedTips.getEntries("RFEnergyStorage").isEmpty()) {
                    if (config.getConfig("thermalexpansion.rfenergybar")) {
                        taggedTips.add(
                                SpecialChars.getRenderString(
                                        "waila.rfenergy",
                                        String.valueOf(energy),
                                        String.valueOf(maxEnergy)),
                                "RFEnergyStorage");
                    } else if (!config.getConfig("thermalexpansion.digitgrouping")) {
                        taggedTips.add(String.format("%d / %d RF", energy, maxEnergy), "RFEnergyStorage");
                    } else {
                        taggedTips.add(
                                String.format(
                                        "%s / %s RF",
                                        energyFormat.format(energy),
                                        energyFormat.format(maxEnergy)),
                                "RFEnergyStorage");
                    }
                }
            }
        } catch (Exception e) {
            currenttip = WailaExceptionHandler.handleErr(e, accessor.getTileEntity().getClass().getName(), currenttip);
        }
        
        return currenttip;
    }

    /** Leaves the tooltip footer unchanged because RF storage is body information. */
    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
        return currenttip;
    }

    /** Synchronizes the current and maximum RF storage values to the client tooltip. */
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
        try {
            Integer energy = -1;
            Integer maxsto = -1;
            if (ThermalExpansionModule.IEnergyInfo.isInstance(te)) {
                energy = (Integer) ThermalExpansionModule.IEnergyInfo_getCurStorage.invoke(te);
                maxsto = (Integer) ThermalExpansionModule.IEnergyInfo_getMaxStorage.invoke(te);
            } else if (ThermalExpansionModule.IEnergyProvider.isInstance(te)) {
                energy = (Integer) ThermalExpansionModule.IEnergyProvider_getCurStorage
                        .invoke(te, ForgeDirection.UNKNOWN);
                maxsto = (Integer) ThermalExpansionModule.IEnergyProvider_getMaxStorage
                        .invoke(te, ForgeDirection.UNKNOWN);
            } else if (ThermalExpansionModule.IEnergyReceiver.isInstance(te)) {
                energy = (Integer) ThermalExpansionModule.IEnergyReceiver_getCurStorage
                        .invoke(te, ForgeDirection.UNKNOWN);
                maxsto = (Integer) ThermalExpansionModule.IEnergyReceiver_getMaxStorage
                        .invoke(te, ForgeDirection.UNKNOWN);
            }

            tag.setInteger("Energy", energy);
            tag.setInteger("MaxStorage", maxsto);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return tag;
    }

}
