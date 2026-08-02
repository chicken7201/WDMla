package com.gtnewhorizons.wdmla.plugin.gregtech;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Exposes the inventory owned by a GregTech base tile's internal meta tile. */
@SuppressWarnings("UnstableApiUsage")
public enum GregTechItemStorageProvider implements IServerExtensionProvider<ItemStack> {

    INSTANCE;

    private static final Cache<Object, ItemCollector<IInventory>> COLLECTORS = CacheBuilder.newBuilder().weakKeys()
            .expireAfterAccess(120, TimeUnit.SECONDS).build();

    /** Collects and merges the visible contents of a GregTech machine inventory. */
    @Nullable
    @Override
    public List<ViewGroup<ItemStack>> getGroups(Accessor accessor) {
        Object target = accessor.getTarget();
        if (target == null) {
            return null;
        }

        try {
            ItemCollector<IInventory> collector = COLLECTORS.get(
                    target,
                    () -> new ItemCollector<>(
                            new ItemIterator.IInventoryItemIterator(GregTechItemStorageProvider::findInventory, 0)));
            return collector.update(accessor);
        } catch (ExecutionException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
            return null;
        }
    }

    /** Resolves the internal meta tile, which implements Minecraft's inventory interface. */
    @Nullable
    private static IInventory findInventory(Accessor accessor) {
        Object target = accessor.getTarget();
        if (target == null) {
            return null;
        }

        try {
            Method getter = target.getClass().getMethod("getMetaTileEntity");
            Object metaTileEntity = getter.invoke(target);
            return metaTileEntity instanceof IInventory inventory ? inventory : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            WailaExceptionHandler.handleErr(exception, GregTechItemStorageProvider.class.getName(), null);
            return null;
        }
    }

    /** Reuses WDMla's standard item-storage client renderer and packet format. */
    @Override
    public net.minecraft.util.ResourceLocation getUid() {
        return Identifiers.ITEM_STORAGE_DEFAULT;
    }
}
