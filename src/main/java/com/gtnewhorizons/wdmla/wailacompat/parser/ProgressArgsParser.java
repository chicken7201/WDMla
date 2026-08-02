package com.gtnewhorizons.wdmla.wailacompat.parser;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;

public class ProgressArgsParser implements ITTRenderParser {

    /** Converts the legacy furnace progress token to WDMla's shared smooth progress bar. */
    @Override
    public Component parse(String[] args) {
        int current = Integer.parseInt(args[0]);
        int maximum = Math.max(1, Integer.parseInt(args[1]));
        int clampedCurrent = Math.max(0, Math.min(current, maximum));
        double ratio = (double) clampedCurrent / maximum;
        ProgressComponent progress = new ProgressComponent(clampedCurrent, maximum);
        progress.child(
                new TextComponent(String.format("%.1f%%", ratio * 100.0))
                        .padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING));
        return progress;
    }
}
