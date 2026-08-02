package com.gtnewhorizons.wdmla.wailacompat;

import java.awt.Dimension;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;

/** Exposes a WDMla component parser through Waila's legacy renderer registry. */
public class ModernRendererAdapter implements IWailaTooltipRenderer {

    private final ITTRenderParser parser;

    /** Stores the native parser used for both measurement and drawing. */
    public ModernRendererAdapter(ITTRenderParser parser) {
        this.parser = parser;
    }

    /** Reports the native component's measured dimensions to legacy Waila callers. */
    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        Component component = parser.parse(params);
        return new Dimension(Math.round(component.getWidth()), Math.round(component.getHeight()));
    }

    /** Draws the native WDMla component at the origin expected by legacy Waila callers. */
    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        parser.parse(params).tick(0.0f, 0.0f);
    }
}
