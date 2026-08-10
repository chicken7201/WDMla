package com.gtnewhorizons.wdmla.plugin.universal;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.gtnewhorizons.wdmla.api.view.ViewGroup;

/** Builds the synchronized model used by single-item stores with an explicit capacity. */
public final class ItemStorageCapacity {

    public static final String STORED = "Stored";
    public static final String CAPACITY = "Capacity";

    /** Prevents construction of the shared item-capacity model helper. */
    private ItemStorageCapacity() {}

    /** Creates one compact item group while preserving the stored stack's item NBT. */
    public static List<ViewGroup<ItemStack>> create(ItemStack storedType, long stored, long capacity) {
        if (storedType == null || storedType.getItem() == null || stored <= 0L || capacity <= 0L) {
            return Collections.emptyList();
        }

        ItemStack displayStack = storedType.copy();
        displayStack.stackSize = 1;
        ViewGroup<ItemStack> group = new ViewGroup<>(Collections.singletonList(displayStack));
        group.getExtraData().setLong(STORED, Math.max(0L, stored));
        group.getExtraData().setLong(CAPACITY, Math.max(0L, capacity));
        return Collections.singletonList(group);
    }
}
