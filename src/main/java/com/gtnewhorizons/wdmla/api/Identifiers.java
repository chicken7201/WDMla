package com.gtnewhorizons.wdmla.api;

import net.minecraft.util.ResourceLocation;

/**
 * WDMla main constants
 * 
 * @see mcp.mobius.waila.utils.Constants
 */
public final class Identifiers {

    // component tag
    public static final ResourceLocation ROOT = Core("root");

    public static final ResourceLocation ITEM_ICON = Core("item_icon");
    public static final ResourceLocation ITEM_NAME = Core("item_name");
    public static final ResourceLocation ENTITY = Core("entity");
    public static final ResourceLocation ENTITY_NAME = Core("entity_name");
    public static final ResourceLocation MOD_NAME = Core("mod_name");
    public static final ResourceLocation BLOCK_FACE = Core("block_face");
    public static final ResourceLocation TARGET_NAME_ROW = Core("target_name_row");

    // sprite location
    public static final ResourceLocation FURNACE_PATH = new ResourceLocation("waila", "textures/sprites.png");
    public static final ResourceLocation RS2_ICON_PATH = new ResourceLocation("waila", "textures/rs2.png");
    public static final ResourceLocation LOCK_PATH = new ResourceLocation("waila", "textures/lock.png");
    public static final ResourceLocation VOID_PATH = new ResourceLocation("waila", "textures/void.png");

    // provider Uid
    public static final ResourceLocation DEFAULT_BLOCK = Core("default_block");
    public static final ResourceLocation DEFAULT_ENTITY = Core("default_entity");
    public static final ResourceLocation ENTITY_HEALTH = Core("entity_health");
    public static final ResourceLocation EQUIPMENT = Core("equipment");
    public static final ResourceLocation ARMOR = Core("armor");
    public static final ResourceLocation ENCHANTMENT_POWER = Core("enchantment_power");
    public static final ResourceLocation STATUS_EFFECT = Core("status_effect");
    public static final ResourceLocation BREAK_PROGRESS_TEXT = Core("break_progress_text");
    public static final ResourceLocation LIGHT_LEVEL = Core("light_level");
    public static final ResourceLocation HIDE_FANCY_ICON = Core("hide_fancy_icon");

    public static final ResourceLocation ITEM_STORAGE = Universal("item_storage");
    public static final ResourceLocation ITEM_STORAGE_DEFAULT = Universal("item_storage_default");
    public static final ResourceLocation FLUID_STORAGE = Universal("fluid_storage");
    public static final ResourceLocation FLUID_STORAGE_DEFAULT = Universal("fluid_storage_default");
    public static final ResourceLocation PROGRESS = Universal("progress");

    public static final ResourceLocation HARDNESS = Debug("hardness");
    public static final ResourceLocation BLAST_RESISTANCE = Debug("blast_resistance");
    public static final ResourceLocation REGISTRY_DATA = Debug("registry_data");
    public static final ResourceLocation COORDINATES = Debug("coordinates");

    public static final ResourceLocation EXAMPLE_HEAD = Example("head");
    public static final ResourceLocation EXAMPLE_NBT_BLOCK = Example("nbt_block");
    public static final ResourceLocation EXAMPLE_ENTITY = Example("entity");
    public static final ResourceLocation EXAMPLE_THEME_BLOCK = Example("theme_block");
    public static final ResourceLocation EXAMPLE_ITEM_STORAGE = Example("item_storage");
    public static final ResourceLocation EXAMPLE_FLUID_STORAGE = Example("fluid_storage");
    public static final ResourceLocation EXAMPLE_PROGRESS = Example("progress");
    public static final ResourceLocation EXAMPLE_HARVEST = Example("harvest");

    public static final String NAMESPACE_CORE = "core";
    public static final String NAMESPACE_UNIVERSAL = "universal";
    public static final String NAMESPACE_DEBUG = "debug";
    public static final String NAMESPACE_EXAMPLE = "example";

    public static final String CONFIG_AUTOGEN = "plugins_autogen";

    public static ResourceLocation Core(String path) {
        return new ResourceLocation(NAMESPACE_CORE, path);
    }

    public static ResourceLocation Universal(String path) {
        return new ResourceLocation(NAMESPACE_UNIVERSAL, path);
    }

    public static ResourceLocation Debug(String path) {
        return new ResourceLocation(NAMESPACE_DEBUG, path);
    }

    public static ResourceLocation Example(String path) {
        return new ResourceLocation(NAMESPACE_EXAMPLE, path);
    }
}
