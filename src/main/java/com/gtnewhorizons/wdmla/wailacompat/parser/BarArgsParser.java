package com.gtnewhorizons.wdmla.wailacompat.parser;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.style.RectStyle;

/** Converts Waila's variable-width bar token into a full-width native WDMla progress bar. */
public class BarArgsParser implements ITTRenderParser {

    private static final int BACKGROUND_COLOR = 0xFF1D1D1D;
    private static final int BORDER_COLOR = 0xFF505050;

    /** Parses the bar label and fill ratio while using WDMla's configured Process Fill colour. */
    @Override
    public Component parse(String[] args) {
        String text = args.length == 0 ? "" : args[0];
        float ratio = parseRatio(args, 3);

        ProgressComponent progress = new ProgressComponent(ratio)
                .rectStyle(new RectStyle().backgroundColor(BACKGROUND_COLOR).borderColor(BORDER_COLOR));
        progress.child(
                new TextComponent(text).padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING));
        return progress;
    }

    /** Parses and clamps the optional progress ratio to the drawable zero-to-one range. */
    static float parseRatio(String[] args, int index) {
        if (index >= args.length) {
            return 1.0f;
        }
        try {
            double ratio = Double.parseDouble(args[index]);
            if (!Double.isFinite(ratio)) {
                return 0.0f;
            }
            return (float) Math.max(0.0, Math.min(1.0, ratio));
        } catch (NumberFormatException ignored) {
            return 1.0f;
        }
    }
}
