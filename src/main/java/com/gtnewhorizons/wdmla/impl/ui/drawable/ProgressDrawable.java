package com.gtnewhorizons.wdmla.impl.ui.drawable;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.wdmla.api.ui.IDrawable;
import com.gtnewhorizons.wdmla.api.ui.IFilledProgress;
import com.gtnewhorizons.wdmla.api.ui.sizer.IArea;
import com.gtnewhorizons.wdmla.api.ui.style.IProgressStyle;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Area;
import com.gtnewhorizons.wdmla.impl.ui.style.ProgressStyle;
import com.gtnewhorizons.wdmla.overlay.GuiDraw;

public class ProgressDrawable implements IDrawable {

    private final @NotNull IFilledProgress progress;
    private @NotNull IProgressStyle style;

    public ProgressDrawable(@NotNull IFilledProgress progress) {
        this.progress = progress;
        this.style = new ProgressStyle();
    }

    public ProgressDrawable style(IProgressStyle style) {
        this.style = style;
        return this;
    }

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
                GuiDraw.drawRect(new Area(area.getX(), area.getY(), dx, area.getH()), style.getFilledColor());
            }
        }
    }
}
