package com.gtnewhorizons.wdmla.plugin.vanilla;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.EntityAccessor;
import com.gtnewhorizons.wdmla.api.provider.IEntityComponentProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

/** Restores WAWLA's witch profession row. */
public enum WitchProfessionProvider implements IEntityComponentProvider {

    INSTANCE;

    /** Displays the localized witch profession for witch entities. */
    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof EntityWitch) {
            tooltip.child(
                    ThemeHelper.INSTANCE.value(
                            StatCollector.translateToLocal("hud.msg.wdmla.profession"),
                            StatCollector.translateToLocal("description.villager.profession.witch")));
        }
    }

    /** Returns the stable configuration identifier for the witch profession provider. */
    @Override
    public ResourceLocation getUid() {
        return VanillaIdentifiers.WITCH_PROFESSION;
    }
}
