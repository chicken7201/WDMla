package com.gtnewhorizons.wdmla.wailacompat;

import java.awt.Dimension;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.wdmla.api.ui.IDrawable;
import com.gtnewhorizons.wdmla.api.ui.sizer.IArea;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Size;

import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.api.IWailaVariableWidthTooltipRenderer;
import mcp.mobius.waila.api.impl.DataAccessorCommon;

/** Preserves third-party registered Waila renderers that do not yet have a native component parser. */
public class LegacyRendererComponent extends Component {

    /** Reserves the renderer's requested size and wraps its origin-relative draw call. */
    public LegacyRendererComponent(IWailaTooltipRenderer renderer, String[] args) {
        super(new Padding(), getRendererSize(renderer, args), new LegacyRendererDrawable(renderer, args));
    }

    /** Converts the legacy AWT dimension into a safe WDMla component size. */
    private static Size getRendererSize(IWailaTooltipRenderer renderer, String[] args) {
        Dimension dimension = renderer.getSize(args, DataAccessorCommon.instance);
        return new Size(Math.max(dimension.width, 0), Math.max(dimension.height, 0));
    }

    /** Draws one registered legacy renderer inside a WDMla-managed area. */
    private static final class LegacyRendererDrawable implements IDrawable {

        private final IWailaTooltipRenderer renderer;
        private final String[] args;

        /** Captures the renderer and its decoded argument array. */
        private LegacyRendererDrawable(IWailaTooltipRenderer renderer, String[] args) {
            this.renderer = renderer;
            this.args = args;
        }

        /** Translates WDMla's absolute area to the legacy renderer's local coordinate system. */
        @Override
        public void draw(IArea area) {
            if (renderer instanceof IWailaVariableWidthTooltipRenderer variableWidthRenderer) {
                variableWidthRenderer.setMaxLineWidth(Math.max(0, Math.round(area.getW())));
            }
            GL11.glPushMatrix();
            try {
                GL11.glTranslatef(area.getX(), area.getY(), 0.0f);
                renderer.draw(args, DataAccessorCommon.instance);
            } finally {
                GL11.glPopMatrix();
            }
        }
    }
}
