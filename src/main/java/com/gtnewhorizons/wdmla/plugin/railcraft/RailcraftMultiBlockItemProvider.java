package com.gtnewhorizons.wdmla.plugin.railcraft;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;
import com.gtnewhorizons.wdmla.plugin.universal.ItemCollector;
import com.gtnewhorizons.wdmla.plugin.universal.ItemIterator;

import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Exposes every formed Railcraft multiblock inventory through WDMla's standard item preview. */
@SuppressWarnings("UnstableApiUsage")
public enum RailcraftMultiBlockItemProvider implements IServerExtensionProvider<ItemStack> {

    INSTANCE;

    private static final Cache<Object, ItemCollector<IInventory>> COLLECTORS = CacheBuilder.newBuilder().weakKeys()
            .expireAfterAccess(120, TimeUnit.SECONDS).build();

    /** Collects items from the authoritative master inventory for any targeted structure part. */
    @Nullable
    @Override
    public List<ViewGroup<ItemStack>> getGroups(Accessor accessor) {
        if (!(accessor.getTarget() instanceof TileMultiBlock)) {
            return null;
        }
        TileMultiBlock master = RailcraftMultiBlockSupport.resolveMaster(accessor);
        IInventory inventory = RailcraftMultiBlockSupport.findInventory(accessor);
        if (master == null || inventory == null) {
            return Collections.emptyList();
        }

        try {
            ItemCollector<IInventory> collector = COLLECTORS.get(
                    master,
                    () -> new ItemCollector<>(
                            new ItemIterator.IInventoryItemIterator(RailcraftMultiBlockSupport::findInventory, 0)));
            return collector.update(accessor);
        } catch (ExecutionException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
            return Collections.emptyList();
        }
    }

    /** Reuses WDMla's standard item-storage packet and client renderer. */
    @Override
    public net.minecraft.util.ResourceLocation getUid() {
        return Identifiers.ITEM_STORAGE_DEFAULT;
    }
}
