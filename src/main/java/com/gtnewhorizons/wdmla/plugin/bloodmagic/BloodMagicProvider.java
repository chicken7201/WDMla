package com.gtnewhorizons.wdmla.plugin.bloodmagic;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayer;
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
import com.gtnewhorizons.wdmla.config.PluginsConfig;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;

import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import WayofTime.alchemicalWizardry.common.tileEntity.TETeleposer;
import WayofTime.alchemicalWizardry.common.tileEntity.TEWritingTable;
import mcp.mobius.waila.Waila;

/** Native WDMla rendering of Blood Magic altar, chemistry, ritual, and teleposer data. */
public enum BloodMagicProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String CURRENT_LP = "BloodMagicCurrentLP";
    private static final String CAPACITY = "BloodMagicCapacity";
    private static final String TIER = "BloodMagicTier";
    private static final String PROGRESS = "BloodMagicProgress";
    private static final String RESULT_STACK = "BloodMagicResultStack";
    private static final String OWNER = "BloodMagicOwner";
    private static final String RITUAL_NAME = "BloodMagicRitual";
    private static Field liquidRequired;

    /** Displays data while preserving WAILAPlugins' Divination/Sight Sigil visibility rules. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound data = accessor.getServerData();
        if (tile instanceof TEAltar) {
            appendAltar(tooltip, accessor.getPlayer(), data);
        } else if (tile instanceof TEWritingTable) {
            appendWritingTable(tooltip, data);
        } else if (tile instanceof TEMasterStone) {
            appendMasterStone(tooltip, data);
        } else if (tile instanceof TETeleposer) {
            ItemStack focus = ItemStack.loadItemStackFromNBT(data.getCompoundTag(RESULT_STACK));
            if (focus != null) {
                tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(focus));
            }
        }
    }

    /** Displays altar LP, capacity, tier, and optionally the active recipe progress gauge. */
    private static void appendAltar(ITooltip tooltip, EntityPlayer player, NBTTagCompound data) {
        SigilAccess access = getSigilAccess(player);
        if (!access.hasDivination()) {
            return;
        }
        tooltip.child(value("hud.msg.wdmla.bloodmagic.current.lp", String.valueOf(data.getInteger(CURRENT_LP))));
        tooltip.child(value("hud.msg.wdmla.capacity", String.valueOf(data.getInteger(CAPACITY))));
        tooltip.child(value("hud.msg.wdmla.tier", String.valueOf(data.getInteger(TIER))));
        if (access.hasSight() && data.hasKey(PROGRESS)) {
            tooltip.progress(
                    data.getInteger(PROGRESS),
                    100,
                    StatCollector.translateToLocal("hud.msg.wdmla.progress"));
        }
    }

    /** Displays chemistry-set progress and its resulting item. */
    private static void appendWritingTable(ITooltip tooltip, NBTTagCompound data) {
        tooltip.progress(
                data.getInteger(PROGRESS),
                100,
                StatCollector.translateToLocal("hud.msg.wdmla.progress"));
        ItemStack result = ItemStack.loadItemStackFromNBT(data.getCompoundTag(RESULT_STACK));
        if (result != null) {
            tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(result));
        }
    }

    /** Displays ritual ownership and the localized ritual name. */
    private static void appendMasterStone(ITooltip tooltip, NBTTagCompound data) {
        String owner = data.getString(OWNER);
        if (!owner.isEmpty()) {
            tooltip.child(value("hud.msg.wdmla.owner", owner));
        }
        String ritual = data.getString(RITUAL_NAME);
        if (!ritual.isEmpty()) {
            tooltip.child(value("hud.msg.wdmla.bloodmagic.ritual", Rituals.getNameOfRitual(ritual)));
        }
    }

    /** Determines which Blood Magic altar fields the current player may inspect. */
    private static SigilAccess getSigilAccess(EntityPlayer player) {
        int requirement = PluginsConfig.wailaPlugins.bloodMagic.sigilRequirement;
        if (requirement == 0) {
            return new SigilAccess(true, true);
        }
        if (requirement == 2) {
            ItemStack held = player.getHeldItem();
            boolean sight = held != null && held.getItem() == ModItems.itemSeerSigil;
            boolean divination = sight || (held != null && held.getItem() == ModItems.divinationSigil);
            return new SigilAccess(divination, sight || !PluginsConfig.wailaPlugins.bloodMagic.seerBenefit);
        }
        boolean sight = findInInventory(ModItems.itemSeerSigil, player);
        boolean divination = sight || findInInventory(ModItems.divinationSigil, player);
        return new SigilAccess(divination, sight || !PluginsConfig.wailaPlugins.bloodMagic.seerBenefit);
    }

    /** Searches normal inventory and Bound Armour's internal sigil inventory. */
    private static boolean findInInventory(Item item, EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() == item) {
                return true;
            }
        }
        for (ItemStack armor : player.inventory.armorInventory) {
            if (armor != null
                    && "WayofTime.alchemicalWizardry.common.items.armour.BoundArmour"
                            .equals(armor.getItem().getClass().getName())) {
                for (ItemStack sigil : getBoundArmourInventory(armor)) {
                    if (sigil != null && sigil.getItem() == item) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Reads Bound Armour's internal sigils without requiring its optional Thaumcraft super-interface. */
    private static ItemStack[] getBoundArmourInventory(ItemStack armor) {
        try {
            Object inventory = armor.getItem().getClass().getMethod("getInternalInventory", ItemStack.class)
                    .invoke(armor.getItem(), armor);
            return inventory instanceof ItemStack[] stacks ? stacks : new ItemStack[0];
        } catch (ReflectiveOperationException e) {
            Waila.log.warn("Unable to read Blood Magic Bound Armour sigils", e);
            return new ItemStack[0];
        }
    }

    /** Collects the minimum Blood Magic state required by the client renderer. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile instanceof TEAltar altar) {
            appendAltarData(data, altar);
        } else if (tile instanceof TEWritingTable writingTable) {
            appendWritingTableData(data, writingTable);
        } else if (tile instanceof TEMasterStone masterStone) {
            data.setString(OWNER, masterStone.getOwner());
            data.setString(RITUAL_NAME, masterStone.getCurrentRitual());
        } else if (tile instanceof TETeleposer teleposer && teleposer.getStackInSlot(0) != null) {
            NBTTagCompound stack = new NBTTagCompound();
            teleposer.getStackInSlot(0).writeToNBT(stack);
            data.setTag(RESULT_STACK, stack);
        }
    }

    /** Collects altar capacity and computes WAILAPlugins' exact percentage formula. */
    private static void appendAltarData(NBTTagCompound data, TEAltar altar) {
        data.setInteger(CURRENT_LP, altar.getCurrentBlood());
        data.setInteger(CAPACITY, altar.getCapacity());
        data.setInteger(TIER, altar.getTier());
        if (!altar.isActive() || altar.getStackInSlot(0) == null) {
            return;
        }
        try {
            Field required = getLiquidRequiredField();
            int maximum = required.getInt(altar) * altar.getStackInSlot(0).stackSize;
            if (maximum > 0) {
                data.setInteger(PROGRESS, (int) (altar.getProgress() * 100D / maximum));
            }
        } catch (ReflectiveOperationException e) {
            Waila.log.warn("Unable to read Blood Magic altar liquid requirement", e);
        }
    }

    /** Resolves and caches Blood Magic's private liquidRequired field. */
    private static Field getLiquidRequiredField() throws NoSuchFieldException {
        if (liquidRequired == null) {
            liquidRequired = TEAltar.class.getDeclaredField("liquidRequired");
            liquidRequired.setAccessible(true);
        }
        return liquidRequired;
    }

    /** Collects chemistry-set progress and resulting stack. */
    private static void appendWritingTableData(NBTTagCompound data, TEWritingTable writingTable) {
        NBTTagCompound serialized = new NBTTagCompound();
        writingTable.writeToNBT(serialized);
        data.setInteger(PROGRESS, serialized.getInteger("progress"));
        ItemStack result = writingTable.getResultingItemStack();
        if (result != null) {
            NBTTagCompound stack = new NBTTagCompound();
            result.writeToNBT(stack);
            data.setTag(RESULT_STACK, stack);
        }
    }

    /** Builds one themed localized key/value row. */
    private static com.gtnewhorizons.wdmla.api.ui.IComponent value(String key, String value) {
        return ThemeHelper.INSTANCE.value(StatCollector.translateToLocal(key), value);
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation("bloodmagic", "wailaplugins_details");
    }

    /** Carries the two tiers of Blood Magic sigil visibility. */
    private static final class SigilAccess {

        private final boolean divination;
        private final boolean sight;

        /** Stores the calculated sigil visibility flags. */
        private SigilAccess(boolean divination, boolean sight) {
            this.divination = divination;
            this.sight = sight;
        }

        /** Returns whether basic altar information is visible. */
        private boolean hasDivination() {
            return divination;
        }

        /** Returns whether advanced altar progress is visible. */
        private boolean hasSight() {
            return sight;
        }
    }
}
