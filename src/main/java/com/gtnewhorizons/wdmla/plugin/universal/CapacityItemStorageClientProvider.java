package com.gtnewhorizons.wdmla.plugin.universal;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IClientExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ClientViewGroup;
import com.gtnewhorizons.wdmla.api.view.ItemView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.util.FormatUtil;

import mcp.mobius.waila.overlay.DisplayUtil;

/** Maps single-item capacity data to the standard small-icon item storage row. */
public enum CapacityItemStorageClientProvider implements IClientExtensionProvider<ItemStack, ItemView> {

    INSTANCE;

    /** Returns the packet format identifier shared by capacity-aware item adapters. */
    @Override
    public ResourceLocation getUid() {
        return Identifiers.ITEM_STORAGE_CAPACITY;
    }

    /** Adds a current/capacity description to every decoded single-item group. */
    @Override
    public List<ClientViewGroup<ItemView>> getClientGroups(Accessor accessor, List<ViewGroup<ItemStack>> groups) {
        return ClientViewGroup.map(groups, ItemView::new, CapacityItemStorageClientProvider::decorateGroup);
    }

    /** Formats one capacity group in the same icon-and-text style used by Storage Drawers. */
    private static void decorateGroup(ViewGroup<ItemStack> source, ClientViewGroup<ItemView> target) {
        long stored = source.getExtraData().getLong(ItemStorageCapacity.STORED);
        long capacity = source.getExtraData().getLong(ItemStorageCapacity.CAPACITY);
        for (ItemView view : target.views) {
            if (view.item == null) {
                continue;
            }
            String displayName = DisplayUtil.stripSymbols(DisplayUtil.itemDisplayNameShortFormatted(view.item));
            HPanelComponent description = new HPanelComponent();
            description.text(displayName + " ")
                    .child(ThemeHelper.INSTANCE.info(FormatUtil.STANDARD.format(stored)))
                    .text(" / ")
                    .text(FormatUtil.STANDARD.format(capacity));
            view.description(description);
        }
    }
}
