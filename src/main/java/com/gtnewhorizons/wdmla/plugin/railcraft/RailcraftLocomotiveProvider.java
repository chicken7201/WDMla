package com.gtnewhorizons.wdmla.plugin.railcraft;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.accessor.EntityAccessor;
import com.gtnewhorizons.wdmla.api.provider.IEntityComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;

import mods.railcraft.common.carts.EntityLocomotiveElectric;
import mods.railcraft.common.carts.EntityLocomotiveSteam;

/** Handles WAILAPlugins' Railcraft locomotive information separately from block providers. */
public enum RailcraftLocomotiveProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    INSTANCE;

    /** Displays electric charge or steam boiler temperature for a locomotive. */
    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor) {
        NBTTagCompound data = accessor.getServerData();
        if (accessor.getEntity() instanceof EntityLocomotiveElectric) {
            RailcraftDetailsProvider.appendCharge(tooltip, data, accessor.getPlayer());
        } else if (accessor.getEntity() instanceof EntityLocomotiveSteam) {
            RailcraftDetailsProvider.appendHeat(tooltip, data);
        }
    }

    /** Collects electric charge or steam boiler heat from the locomotive. */
    @Override
    public void appendServerData(NBTTagCompound data, EntityAccessor accessor) {
        Entity entity = accessor.getEntity();
        if (entity instanceof EntityLocomotiveElectric electric) {
            data.setDouble("RailcraftCharge", electric.getChargeHandler().getCharge());
        } else if (entity instanceof EntityLocomotiveSteam steam) {
            data.setFloat("RailcraftHeat", (float) steam.boiler.getHeat());
            data.setFloat("RailcraftMaxHeat", (float) steam.boiler.getMaxHeat());
        }
    }

    /** Returns the stable locomotive provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("railcraft", "wailaplugins_locomotive");
    }
}
