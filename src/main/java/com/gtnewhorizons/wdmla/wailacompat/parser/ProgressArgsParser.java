package com.gtnewhorizons.wdmla.wailacompat.parser;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.IconComponent;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.overlay.WDMlaUIIcons;

public class ProgressArgsParser implements ITTRenderParser {

    /** Converts the legacy furnace progress token to WDMla's clipped furnace sprite. */
    @Override
    public Component parse(String[] args) {
        int current = Integer.parseInt(args[0]);
        int max = Integer.parseInt(args[1]);
        float ratio = max <= 0 ? 0.0f : Math.max(0.0f, Math.min((float) current / max, 1.0f));
        HPanelComponent hPanel = new HPanelComponent();
        hPanel.padding(new Padding().horizontal(2)).child(
                new IconComponent(WDMlaUIIcons.FURNACE_BG, WDMlaUIIcons.FURNACE_BG.texPath).padding(new Padding())
                        .child(
                                new IconComponent(WDMlaUIIcons.FURNACE, WDMlaUIIcons.FURNACE.texPath)
                                        .clip(0f, 0f, ratio, 1f).padding(new Padding())));
        return hPanel;
    }
}
