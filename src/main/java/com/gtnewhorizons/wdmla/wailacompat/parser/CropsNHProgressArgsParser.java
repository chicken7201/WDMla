package com.gtnewhorizons.wdmla.wailacompat.parser;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;

/** Converts CropsNH's legacy crop renderer into a modern, smoothly animated WDMla progress bar. */
public class CropsNHProgressArgsParser implements ITTRenderParser {

    /** Parses current, maximum, and scan-state arguments through WDMla's shared smooth progress component. */
    @Override
    public Component parse(String[] args) {
        int current = Integer.parseInt(args[0]);
        int maximum = Math.max(1, Integer.parseInt(args[1]));
        int clampedCurrent = Math.max(0, Math.min(current, maximum));
        double rawRatio = (double) clampedCurrent / maximum;

        ProgressComponent progress = new ProgressComponent(clampedCurrent, maximum);
        progress.child(
                new TextComponent(buildText(clampedCurrent, maximum, rawRatio, "1".equals(args[2])))
                        .padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING));
        return progress;
    }

    /** Builds the original CropsNH scanned or percentage-only label for the modern bar. */
    private static String buildText(int current, int maximum, double ratio, boolean scanned) {
        if (scanned) {
            return StatCollector.translateToLocalFormatted(
                    "cropsnh_tooltip.waila.cropStick.progressBar.scanned",
                    formatNumber(current),
                    formatNumber(maximum),
                    ratio * 100.0);
        }
        return StatCollector.translateToLocalFormatted(
                "cropsnh_tooltip.waila.cropStick.progressBar.notScanned",
                ratio * 100.0);
    }

}
