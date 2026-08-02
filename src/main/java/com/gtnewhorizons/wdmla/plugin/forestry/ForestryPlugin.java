package com.gtnewhorizons.wdmla.plugin.forestry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.config.WDMlaConfig;

import forestry.apiculture.blocks.BlockBeehives;
import forestry.apiculture.multiblock.TileAlveary;
import forestry.arboriculture.tiles.TileTreeContainer;
import forestry.core.tiles.TileForestry;
import mcp.mobius.waila.Waila;

@WDMlaPlugin(uid = "forestry", dependencies = "Forestry")
public class ForestryPlugin implements IWDMlaPlugin {

    /** Registers Forestry's harvest rule and all WAILAPlugins client details. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerHarvest(ForestryToolHarvestHandler.INSTANCE, BlockBeehives.class);
        registration.registerBlockComponent(ForestryDetailsProvider.INSTANCE, net.minecraft.block.Block.class);

        WDMlaConfig.instance().getCategory(Identifiers.CONFIG_AUTOGEN + Configuration.CATEGORY_SPLITTER + "forestry")
                .setLanguageKey("provider.wdmla.forestry.category");
    }

    /** Registers Forestry and optional MagicBees server-data targets. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerBlockDataProvider(ForestryDetailsProvider.INSTANCE, TileForestry.class);
        registration.registerBlockDataProvider(ForestryDetailsProvider.INSTANCE, TileTreeContainer.class);
        registration.registerBlockDataProvider(ForestryDetailsProvider.INSTANCE, TileAlveary.class);
        try {
            registration.registerBlockDataProvider(
                    ForestryDetailsProvider.INSTANCE,
                    Class.forName("magicbees.tileentity.TileEntityMagicApiary"));
        } catch (ClassNotFoundException e) {
            Waila.log.debug("MagicBees apiary is not present; skipping its Forestry detail provider");
        }
    }

    /** Builds a provider identifier in Forestry's namespace. */
    public static ResourceLocation path(String path) {
        return new ResourceLocation("forestry", path);
    }
}
