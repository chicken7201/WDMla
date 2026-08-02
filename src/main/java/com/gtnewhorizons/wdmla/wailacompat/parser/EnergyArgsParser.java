package com.gtnewhorizons.wdmla.wailacompat.parser;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.api.ui.ColorPalette;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.style.ProgressStyle;

public class EnergyArgsParser implements ITTRenderParser {

    /** Converts Waila's RF storage token into WDMla's modern striped energy bar. */
    @Override
    public Component parse(String[] args) {
        long amount = Long.parseLong(args[0]);
        long capacity = Long.parseLong(args[1]);
        long progressMax = Math.max(Math.max(capacity, amount), 0L);
        long progress = Math.max(0L, Math.min(amount, progressMax));

        ProgressComponent component = new ProgressComponent(progress, progressMax)
                .style(
                        new ProgressStyle().color(
                                ColorPalette.ENERGY_FILLED,
                                ColorPalette.ENERGY_FILLED_ALTERNATE));
        component.child(
                new TextComponent(String.format("%s / %s RF", formatNumber(amount), formatNumber(capacity)))
                        .padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING));
        return component;
    }
}
