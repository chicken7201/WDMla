package com.gtnewhorizons.wdmla.impl.ui.drawable;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.wdmla.api.ui.IDrawable;
import com.gtnewhorizons.wdmla.api.ui.IFilledProgress;
import com.gtnewhorizons.wdmla.api.ui.sizer.IArea;
import com.gtnewhorizons.wdmla.api.ui.style.IProgressStyle;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Area;
import com.gtnewhorizons.wdmla.impl.ui.style.ProgressStyle;
import com.gtnewhorizons.wdmla.overlay.GuiDraw;
import com.gtnewhorizons.wdmla.util.Color;

public class ProgressDrawable implements IDrawable {

    private final @NotNull IFilledProgress progress;
    private @NotNull IProgressStyle style;

    /** Creates a drawable backed by the supplied current and maximum progress values. */
    public ProgressDrawable(@NotNull IFilledProgress progress) {
        this.progress = progress;
        this.style = new ProgressStyle();
    }

    /** Applies the fill appearance used on subsequent draws. */
    public ProgressDrawable style(IProgressStyle style) {
        this.style = style;
        return this;
    }

    /** Draws a smooth vertical fill gradient without striped indicators. */
    @Override
    public void draw(IArea area) {
        long current = progress.getCurrent();
        long max = progress.getMax();
        if (current <= 0L || max <= 0L) {
            return;
        }

        float dx = Math.min(current * area.getW() / max, area.getW());
        if (dx > 0) {
            if (style.getOverlay() != null) {
                style.getOverlay().draw(new Area(area.getX(), area.getY(), dx, area.getH()));
            } else {
                fillWithGradient(area, dx);
            }
        }
    }

    /** Shades single-colour fills subtly and blends multi-colour fills such as Energy without stripes. */
    private void fillWithGradient(IArea area, float dx) {
        int filledColor = style.getFilledColor();
        int bottomColor = style.getAlternateFilledColor();
        if (filledColor == bottomColor) {
            bottomColor = Color.setLightness(filledColor, 0.8f);
        }
        GuiDraw.drawStraightGradientRect(
                new Area(area.getX(), area.getY(), dx, area.getH()),
                filledColor,
                bottomColor,
                false);
    }
}
