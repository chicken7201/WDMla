package com.gtnewhorizons.wdmla.plugin.ae2;

import net.minecraft.block.Block;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;

import mcp.mobius.waila.Waila;

/** Registers bounded local-cell summaries for AE2 storage blocks. */
@WDMlaPlugin(uid = "ae2_storage", dependencies = "appliedenergistics2")
public class AE2StoragePlugin implements IWDMlaPlugin {

    private static final String BLOCK_CHEST = "appeng.block.storage.BlockChest";
    private static final String BLOCK_DRIVE = "appeng.block.storage.BlockDrive";
    private static final String TILE_CHEST = "appeng.tile.storage.TileChest";
    private static final String TILE_DRIVE = "appeng.tile.storage.TileDrive";

    private final AE2StorageSummaryProvider provider = AE2StorageSummaryProvider.create();

    /** Registers server summaries on the exact ME Chest and ME Drive tile classes. */
    @Override
    public void register(IWDMlaCommonRegistration registration) {
        if (provider == null) {
            return;
        }
        registerServer(registration, TILE_CHEST);
        registerServer(registration, TILE_DRIVE);
    }

    /** Registers client rendering only on the exact ME Chest and ME Drive block classes. */
    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        if (provider == null) {
            return;
        }
        registerClient(registration, BLOCK_CHEST);
        registerClient(registration, BLOCK_DRIVE);
    }

    /** Resolves and registers one optional AE2 tile class. */
    private void registerServer(IWDMlaCommonRegistration registration, String className) {
        try {
            registration.registerBlockDataProvider(provider, Class.forName(className));
        } catch (ClassNotFoundException exception) {
            Waila.log.warn("Unable to register AE2 storage summary for {}", className, exception);
        }
    }

    /** Resolves and registers one optional AE2 block class. */
    private void registerClient(IWDMlaClientRegistration registration, String className) {
        try {
            registration.registerBlockComponent(provider, Class.forName(className).asSubclass(Block.class));
        } catch (ClassNotFoundException exception) {
            Waila.log.warn("Unable to register AE2 storage summary renderer for {}", className, exception);
        }
    }
}
