package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.TooltipPosition;
import com.gtnewhorizons.wdmla.api.accessor.EntityAccessor;
import com.gtnewhorizons.wdmla.api.provider.IEntityComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

/** Changes a morphed player's WDMla header to the represented entity. */
public enum MorphWawlaProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    INSTANCE;

    private static final String MORPH_ID = "WawlaMorphID";

    /** Replaces the entity title and 3D icon with the synchronized Morph entity. */
    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor) {
        String morphId = accessor.getServerData().getString(MORPH_ID);
        if (morphId.isEmpty()) {
            return;
        }
        Entity morph = EntityList.createEntityByName(morphId, accessor.getWorld());
        if (morph != null) {
            ThemeHelper.INSTANCE.overrideEntityTooltipTitle(tooltip, morph.getCommandSenderName(), morph);
            ThemeHelper.INSTANCE.overrideEntityTooltipIcon(tooltip, morph);
        }
    }

    /** Extracts Morph's nested target entity id without linking against Morph classes. */
    @Override
    public void appendServerData(NBTTagCompound data, EntityAccessor accessor) {
        NBTTagCompound current = accessor.getEntity().getEntityData();
        String[] path = { "PlayerPersisted", "MorphSave", "morphData", "nextState", "entInstanceTag" };
        for (String key : path) {
            if (!current.hasKey(key, 10)) {
                return;
            }
            current = current.getCompoundTag(key);
        }
        if (current.hasKey("id", 8)) {
            data.setString(MORPH_ID, current.getString("id"));
        }
    }

    /** Returns the header priority needed to replace the default player presentation. */
    @Override
    public int getDefaultPriority() {
        return TooltipPosition.HEAD + 10;
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("morph", "wawla_entity_override");
    }
}
