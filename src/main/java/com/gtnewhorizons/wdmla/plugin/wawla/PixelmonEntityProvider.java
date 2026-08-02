package com.gtnewhorizons.wdmla.plugin.wawla;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.accessor.EntityAccessor;
import com.gtnewhorizons.wdmla.api.provider.IEntityComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

/** Reimplements WAWLA's complete Pixelmon entity details without a compile-time Pixelmon dependency. */
public enum PixelmonEntityProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    INSTANCE;

    /** Displays nature, ability, growth size, friendship, held item, IVs, and EVs on Show Details. */
    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor) {
        if (!accessor.showDetails()) {
            return;
        }
        NBTTagCompound data = accessor.getServerData();
        tooltip.child(value("hud.msg.wdmla.pixelmon.nature", enumName("EnumNature", data.getShort("Nature"))));
        tooltip.child(value("hud.msg.wdmla.pixelmon.ability", data.getString("Ability")));
        tooltip.child(value("hud.msg.wdmla.pixelmon.size", enumName("EnumGrowth", data.getShort("Growth"))));
        tooltip.child(value("hud.msg.wdmla.pixelmon.friendship", String.valueOf(data.getInteger("Friendship"))));
        if (data.hasKey("HeldItemStack", 10)) {
            ItemStack held = ItemStack.loadItemStackFromNBT(data.getCompoundTag("HeldItemStack"));
            if (held != null) {
                tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(held));
            }
        }
        tooltip.child(value("hud.msg.wdmla.pixelmon.iv", formatStats(data, "IV")));
        if (!data.getString("OwnerUUID").isEmpty()) {
            tooltip.child(value("hud.msg.wdmla.pixelmon.ev", formatStats(data, "EV")));
        }
    }

    /** Converts Pixelmon's nature and growth enum index into the enum's own display string. */
    private static String enumName(String simpleName, int index) {
        try {
            Class<?> enumClass = Class.forName("com.pixelmonmod.pixelmon.enums." + simpleName);
            Object[] constants = enumClass.getEnumConstants();
            return index >= 0 && index < constants.length ? constants[index].toString() : "?";
        } catch (ClassNotFoundException e) {
            return "?";
        }
    }

    /** Formats Pixelmon's six IV or EV values in the same order as WAWLA. */
    private static String formatStats(NBTTagCompound data, String prefix) {
        return String.format(
                "Atk %d | Def %d | HP %d | SpA %d | SpD %d | Spe %d",
                data.getInteger(prefix + "Attack"),
                data.getInteger(prefix + "Defence"),
                data.getInteger(prefix + "HP"),
                data.getInteger(prefix + "SpAtt"),
                data.getInteger(prefix + "SpDef"),
                data.getInteger(prefix + "Speed"));
    }

    /** Synchronizes Pixelmon's standard entity NBT exactly as WAWLA did. */
    @Override
    public void appendServerData(NBTTagCompound data, EntityAccessor accessor) {
        accessor.getEntity().writeToNBT(data);
    }

    /** Builds a themed localized key/value row. */
    private static com.gtnewhorizons.wdmla.api.ui.IComponent value(String key, String value) {
        return ThemeHelper.INSTANCE.value(StatCollector.translateToLocal(key), value);
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("pixelmon", "wawla_entity_details");
    }
}
