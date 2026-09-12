package mcp.mobius.waila.overlay.tooltiprenderers;

import java.awt.Dimension;

import net.minecraft.client.gui.Gui;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaVariableWidthTooltipRenderer;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.overlay.DisplayUtil;

/**
 * Preserves the legacy variable-width bar API used by current GregTech Waila providers.
 */
public class TTRenderBar implements IWailaVariableWidthTooltipRenderer {

    private static final int HEIGHT = 12;

    private int maxStringW;

    /** Returns the minimum size needed for the bar label. */
    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        String text = params[0];
        int displayWidth = DisplayUtil.getDisplayWidth(text);
        return new Dimension(displayWidth + 4, HEIGHT);
    }

    /** Draws the legacy gradient bar using its encoded text, colours, and fill ratio. */
    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        String text = params[0];
        int topColor = 0xFFFFEE55;
        int bottomColor = 0xFFFFA500;
        int fillWidth = maxStringW - 2;

        if (params.length > 2) {
            try {
                topColor = Integer.parseInt(params[1]);
                bottomColor = Integer.parseInt(params[2]);
            } catch (NumberFormatException ignored) {}
        }

        if (params.length > 3) {
            try {
                double ratio = Double.parseDouble(params[3]);
                ratio = Math.max(0.0, Math.min(1.0, ratio));
                fillWidth = (int) ((maxStringW - 2) * ratio);
            } catch (NumberFormatException ignored) {}
        }

        drawVerticalGradient(maxStringW - 1, 0xFF2A2A2A, 0xFF111111);
        if (fillWidth > 0) {
            drawVerticalGradient(1 + fillWidth, topColor, bottomColor);
        }

        DisplayUtil.drawThickBeveledBox(0, 0, maxStringW, HEIGHT, 1, 0xFF505050, 0xFF505050, -1);
        DisplayUtil.drawString(text, 2, 2, 0xFFFFFFFF, true);
    }

    /** Stores the line width supplied by the enclosing tooltip layout. */
    @Override
    public void setMaxLineWidth(int width) {
        maxStringW = width;
    }

    /** Returns the line width most recently supplied by the tooltip layout. */
    @Override
    public int getMaxLineWidth() {
        return maxStringW;
    }

    /** Draws a vertical colour gradient from the left edge to the requested width. */
    private void drawVerticalGradient(int x2, int topColor, int bottomColor) {
        int height = HEIGHT - 1;
        if (x2 <= 1) {
            return;
        }

        for (int y = 0; y < height; y++) {
            float ratio = (float) y / (float) height;
            int color = blendColors(topColor, bottomColor, ratio);
            Gui.drawRect(1, y, x2, y + 1, color);
        }
    }

    /** Blends two ARGB colours by the supplied ratio. */
    private int blendColors(int first, int second, float ratio) {
        int firstAlpha = first >> 24 & 0xFF;
        int firstRed = first >> 16 & 0xFF;
        int firstGreen = first >> 8 & 0xFF;
        int firstBlue = first & 0xFF;
        int secondAlpha = second >> 24 & 0xFF;
        int secondRed = second >> 16 & 0xFF;
        int secondGreen = second >> 8 & 0xFF;
        int secondBlue = second & 0xFF;

        int alpha = (int) (firstAlpha + (secondAlpha - firstAlpha) * ratio);
        int red = (int) (firstRed + (secondRed - firstRed) * ratio);
        int green = (int) (firstGreen + (secondGreen - firstGreen) * ratio);
        int blue = (int) (firstBlue + (secondBlue - firstBlue) * ratio);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    /** Encodes a two-colour legacy bar token for Waila tooltip transport. */
    public static String create(String text, int topColor, int bottomColor, double progress) {
        return SpecialChars.getRenderString(
                "waila.bar",
                text,
                String.valueOf(topColor),
                String.valueOf(bottomColor),
                String.valueOf(progress));
    }

    /** Encodes a single-colour legacy bar token for Waila tooltip transport. */
    public static String create(String text, int color, double progress) {
        return create(text, color, color, progress);
    }
}
