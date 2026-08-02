package com.gtnewhorizons.wdmla.wailacompat.parser;

import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.IconComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.VPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Size;
import com.gtnewhorizons.wdmla.impl.ui.style.PanelStyle;
import com.gtnewhorizons.wdmla.overlay.WDMlaUIIcons;

public class GTProgressArgsParser implements ITTRenderParser {

    /** Converts GregTech's three-argument Waila progress token into WDMla progress and status components. */
    @Override
    public Component parse(String[] args) {
        int progressTime = Integer.parseInt(args[0]);
        int maxProgressTime = Integer.parseInt(args[1]);
        boolean allowedToWork = Boolean.parseBoolean(args[2]);
        double ratio = maxProgressTime == 0 ? 0.0 : (double) progressTime / maxProgressTime;
        ratio = Math.max(0.0, Math.min(ratio, 1.0));

        String progressText = StatCollector.translateToLocalFormatted(
                "GT5U.waila.machine.in_progress",
                progressTime / 20.0,
                maxProgressTime / 20.0,
                Math.round(ratio * 1000.0) / 10.0);
        VPanelComponent result = new VPanelComponent();
        result.style(new PanelStyle().spacing(2));
        result.child(
                new ProgressComponent((float) ratio).child(
                        new TextComponent(progressText)
                                .padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING)));
        if (!allowedToWork) {
            result.child(buildDisabledStatus());
        }
        return result;
    }

    /** Builds the modern pause-icon row used when GregTech working is disabled. */
    private static Component buildDisabledStatus() {
        HPanelComponent status = new HPanelComponent();
        status.style(new PanelStyle().spacing(2));
        status.child(
                new IconComponent(WDMlaUIIcons.PAUSE, WDMlaUIIcons.PAUSE.texPath).padding(new Padding())
                        .size(new Size(9, 9)));
        status.child(new TextComponent(StatCollector.translateToLocal("GT5U.waila.machine.working_disabled")));
        return status;
    }
}
