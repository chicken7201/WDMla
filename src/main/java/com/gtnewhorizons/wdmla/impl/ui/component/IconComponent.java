package com.gtnewhorizons.wdmla.impl.ui.component;

import java.util.ArrayList;

import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.impl.ui.drawable.IconDrawable;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Size;
import com.gtnewhorizons.wdmla.impl.ui.value.ProgressAnimationTracker;

public class IconComponent extends TooltipComponent {

    public IconComponent(IIcon icon, ResourceLocation path) {
        super(
                new ArrayList<>(),
                new Padding(-1, 0, 0, 0),
                new Size(icon.getIconWidth(), icon.getIconHeight()),
                new IconDrawable(icon, path));
    }

    public IconComponent clip(float suRatio, float svRatio, float twRatio, float thRatio) {
        ((IconDrawable) foreground).clip(suRatio, svRatio, twRatio, thRatio);
        return this;
    }

    /** Clips this icon horizontally using the shared smooth progress tracker. */
    public IconComponent clipProgress(long current, long maximum) {
        long safeMaximum = Math.max(1L, maximum);
        long safeCurrent = Math.max(0L, Math.min(current, safeMaximum));
        ((IconDrawable) foreground).clipProgress(ProgressAnimationTracker.track(safeCurrent, safeMaximum));
        return this;
    }
}
