package mcp.mobius.waila.api;

import java.awt.Dimension;

/**
 * Interface for tooltip renderers that adjust their width depending on the width of the rest of the tooltip.
 * setMaxLineWidth is called by the tooltip before every render to provide the longest line width.
 */
public interface IWailaVariableWidthTooltipRenderer extends IWailaTooltipRenderer {

    /** Returns the minimum area reserved for this renderer. */
    Dimension getSize(String[] params, IWailaCommonAccessor accessor);

    /** Supplies the width of the longest line in the current tooltip. */
    void setMaxLineWidth(int width);

    /** Returns the width of the longest line in the current tooltip. */
    int getMaxLineWidth();
}
