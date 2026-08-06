package mcp.mobius.waila.api;

import java.util.LinkedHashMap;

import net.minecraft.item.ItemStack;

public interface IWailaSummaryProvider {

    /** Returns summary entries for the supplied item stack. */
    LinkedHashMap<String, String> getSummary(ItemStack stack, LinkedHashMap<String, String> currentSummary,
            IWailaConfigHandler config);
}
