package com.gtnewhorizons.wdmla.plugin.gregtech;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;
import com.gtnewhorizons.wdmla.plugin.universal.ItemStorageCapacity;

import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Reads only GregTech's dedicated digital chest hierarchy, never arbitrary machine inventories. */
public final class GregTechItemStorageProvider implements IServerExtensionProvider<ItemStack> {

    private static final String DIGITAL_CHEST = "gregtech.common.tileentities.storage.MTEDigitalChestBase";

    private final Class<?> digitalChestClass;
    private final Method getMetaTileEntity;
    private final Method getItemStack;
    private final Method getItemCount;
    private final Method getMaxItemCount;
    private final Method getStackInSlot;

    /** Resolves and caches GregTech's public storage methods once during plugin registration. */
    private GregTechItemStorageProvider(Class<?> baseMetaTileEntity) throws ReflectiveOperationException {
        digitalChestClass = Class.forName(DIGITAL_CHEST);
        getMetaTileEntity = baseMetaTileEntity.getMethod("getMetaTileEntity");
        getItemStack = digitalChestClass.getMethod("getItemStack");
        getItemCount = digitalChestClass.getMethod("getItemCount");
        getMaxItemCount = digitalChestClass.getMethod("getMaxItemCount");
        getStackInSlot = digitalChestClass.getMethod("getStackInSlot", int.class);
    }

    /** Creates the exact digital-chest adapter when the installed GregTech API matches. */
    @Nullable
    public static GregTechItemStorageProvider create(Class<?> baseMetaTileEntity) {
        try {
            return new GregTechItemStorageProvider(baseMetaTileEntity);
        } catch (ReflectiveOperationException exception) {
            WailaExceptionHandler.handleErr(exception, GregTechItemStorageProvider.class.getName(), null);
            return null;
        }
    }

    /** Reads the stored prototype, true count, and tier-dependent maximum from a digital chest. */
    @Nullable
    @Override
    public List<ViewGroup<ItemStack>> getGroups(Accessor accessor) {
        Object target = accessor.getTarget();
        if (target == null) {
            return null;
        }

        try {
            Object metaTileEntity = getMetaTileEntity.invoke(target);
            if (!digitalChestClass.isInstance(metaTileEntity)) {
                return null;
            }

            ItemStack storedType = (ItemStack) getItemStack.invoke(metaTileEntity);
            long stored = ((Number) getItemCount.invoke(metaTileEntity)).longValue();
            long capacity = ((Number) getMaxItemCount.invoke(metaTileEntity)).longValue();
            ItemStack output = (ItemStack) getStackInSlot.invoke(metaTileEntity, 1);
            if (sameItem(storedType, output)) {
                stored += output.stackSize;
            }
            return ItemStorageCapacity.create(storedType, stored, capacity);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
            return null;
        }
    }

    /** Compares the item identity, damage, and NBT used by GregTech's output buffer. */
    private static boolean sameItem(@Nullable ItemStack first, @Nullable ItemStack second) {
        return first != null && second != null && first.isItemEqual(second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    /** Selects the shared capacity-aware item client renderer. */
    @Override
    public ResourceLocation getUid() {
        return Identifiers.ITEM_STORAGE_CAPACITY;
    }

    /** Ensures the exact digital chest adapter wins over generic storage adapters. */
    @Override
    public int getDefaultPriority() {
        return 1000;
    }
}
