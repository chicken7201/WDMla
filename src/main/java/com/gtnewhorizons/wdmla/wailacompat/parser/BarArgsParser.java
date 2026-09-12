package com.gtnewhorizons.wdmla.wailacompat.parser;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.style.ProgressStyle;
import com.gtnewhorizons.wdmla.impl.ui.style.RectStyle;

/** Converts Waila's variable-width bar token into a full-width native WDMla progress bar. */
public class BarArgsParser implements ITTRenderParser {

    static final int DEFAULT_TOP_COLOR = 0xFFFFEE55;
    static final int DEFAULT_BOTTOM_COLOR = 0xFFFFA500;
    private static final int BACKGROUND_COLOR = 0xFF1D1D1D;
    private static final int BORDER_COLOR = 0xFF505050;

    /** Parses the bar label, colours, and fill ratio while accepting omitted legacy options. */
    @Override
    public Component parse(String[] args) {
        String text = args.length == 0 ? "" : args[0];
        int topColor = parseColor(args, 1, DEFAULT_TOP_COLOR);
        int bottomColor = parseColor(args, 2, DEFAULT_BOTTOM_COLOR);
        float ratio = parseRatio(args, 3);

        ProgressComponent progress = new ProgressComponent(ratio)
                .style(new ProgressStyle().color(topColor, bottomColor))
                .rectStyle(new RectStyle().backgroundColor(BACKGROUND_COLOR).borderColor(BORDER_COLOR));
        progress.child(
                new TextComponent(text).padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING));
        return progress;
    }

    /** Parses one optional signed ARGB colour and falls back when the value is invalid. */
    static int parseColor(String[] args, int index, int fallback) {
        if (index >= args.length) {
            return fallback;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
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
