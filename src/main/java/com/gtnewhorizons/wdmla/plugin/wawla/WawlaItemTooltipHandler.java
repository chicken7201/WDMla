package com.gtnewhorizons.wdmla.plugin.wawla;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.gtnewhorizons.wdmla.WDMla;
import com.gtnewhorizons.wdmla.util.FormatUtil;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** Reimplements WAWLA's inventory tooltip additions without requiring the WAWLA mod. */
public enum WawlaItemTooltipHandler {

    INSTANCE;

    /** Adds enchantment descriptions, armor protection, enchant power, and development registry names. */
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null || !event.entityPlayer.worldObj.isRemote) {
            return;
        }

        Item item = event.itemStack.getItem();
        if (item instanceof ItemEnchantedBook) {
            appendEnchantmentDescription(event);
        } else if (item instanceof ItemArmor armor) {
            event.toolTip.add(
                    StatCollector.translateToLocal("hud.msg.wdmla.armor.protection") + ": "
                            + armor.damageReduceAmount);
        }

        Block block = Block.getBlockFromItem(item);
        if (block != null && !(block instanceof BlockAir)) {
            float enchantPower = block.getEnchantPowerBonus(event.entityPlayer.worldObj, 0, 0, 0);
            if (enchantPower > 0) {
                event.toolTip.add(
                        StatCollector.translateToLocal("hud.msg.wdmla.enchantment.power") + ": "
                                + FormatUtil.STANDARD.format(enchantPower));
            }
        }

        if (WDMla.isDevEnv()) {
            event.toolTip.add(Item.itemRegistry.getNameForObject(item));
        }
    }

    /** Adds the first stored enchantment's localized description while the sneak key is held. */
    private static void appendEnchantmentDescription(ItemTooltipEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!minecraft.gameSettings.isKeyDown(minecraft.gameSettings.keyBindSneak)) {
            event.toolTip.add(StatCollector.translateToLocal("hud.msg.wdmla.enchantment.hold.sneak"));
            return;
        }

        Enchantment enchantment = getFirstStoredEnchantment(event.itemStack.getTagCompound());
        if (enchantment == null) {
            return;
        }
        String key = "description." + enchantment.getName();
        String description = StatCollector.translateToLocal(key);
        if (description.equals(key)) {
            event.toolTip.add(StatCollector.translateToLocal("hud.msg.wdmla.enchantment.description.missing"));
            return;
        }
        List<String> wrapped = minecraft.fontRenderer.listFormattedStringToWidth(description, 225);
        event.toolTip.addAll(wrapped);
    }

    /** Resolves the first valid enchantment from an enchanted book's StoredEnchantments tag. */
    private static Enchantment getFirstStoredEnchantment(NBTTagCompound tag) {
        if (tag == null) {
            return null;
        }
        NBTTagList enchantments = tag.getTagList("StoredEnchantments", 10);
        if (enchantments.tagCount() == 0) {
            return null;
        }
        int id = enchantments.getCompoundTagAt(0).getShort("id");
        return id >= 0 && id < Enchantment.enchantmentsList.length ? Enchantment.enchantmentsList[id] : null;
    }
}
