package com.gtnewhorizons.wdmla.plugin.tconstruct;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.FluidView;

/** Shares the built-in FluidView client decoder with TConstruct-specific server providers. */
public interface TConstructFluidStorageProvider extends IServerExtensionProvider<FluidView.Data> {

    /** Uses WDMla's standard fluid decoder for the synchronized TConstruct tank data. */
    @Override
    default ResourceLocation getUid() {
        return Identifiers.FLUID_STORAGE_DEFAULT;
    }

    /** Prioritizes the exact TConstruct provider over the generic Block fallback. */
    @Override
    default int getDefaultPriority() {
        return 1000;
    }
}
