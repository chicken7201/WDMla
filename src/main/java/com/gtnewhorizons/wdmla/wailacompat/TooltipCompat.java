package com.gtnewhorizons.wdmla.wailacompat;

import static mcp.mobius.waila.api.SpecialChars.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.VPanelComponent;
import com.gtnewhorizons.wdmla.wailacompat.parser.AspectArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.CropsNHProgressArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.EnergyArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.FluidArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.GTProgressArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.HealthArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.IconArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.ItemArgsParser;
import com.gtnewhorizons.wdmla.wailacompat.parser.ProgressArgsParser;

import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.overlay.DisplayUtil;

/**
 * Replacement of old computeRenderables from Waila Tooltip class.
 */
public class TooltipCompat {

    private final ITTRenderParser healthParser = new HealthArgsParser();
    private final ITTRenderParser itemParser = new ItemArgsParser();
    private final ITTRenderParser progressParser = new ProgressArgsParser();
    private final ITTRenderParser fluidParser = new FluidArgsParser();
    private final ITTRenderParser energyParser = new EnergyArgsParser();
    private final ITTRenderParser aspectParser = new AspectArgsParser();
    private final ITTRenderParser gtProgressParser = new GTProgressArgsParser();
    private final ITTRenderParser iconParser = new IconArgsParser();
    private final ITTRenderParser cropsNHProgressParser = new CropsNHProgressArgsParser();

    /** Converts every legacy Waila row into modern WDMla components. */
    public ITooltip computeRenderables(List<String> legacyTextData) {
        ITooltip verticalLayout = new VPanelComponent();

        ArrayList<ArrayList<String>> lines = new ArrayList<>();
        for (String s : legacyTextData) {

            ArrayList<String> line = new ArrayList<>(Arrays.asList(patternTab.split(s)));
            lines.add(line);
        }

        for (int i = 0; i < lines.size(); i++) {
            ITooltip lineComponent = verticalLayout.horizontal();
            for (int c = 0; c < lines.get(i).size(); c++) { // We check all the columns for this line
                String currentLine = lines.get(i).get(c);
                Matcher lineMatcher = patternLineSplit.matcher(currentLine);

                while (lineMatcher.find()) {
                    String cs = lineMatcher.group();
                    Matcher renderMatcher = patternRender.matcher(cs); // We keep a matcher here to be able to check if
                    // we have a Renderer. Might be better to do a
                    // startWith + full matcher init after the check
                    Matcher iconMatcher = patternIcon.matcher(cs);

                    if (renderMatcher.find()) {
                        String renderName = renderMatcher.group("name");
                        String[] rendererArgs = splitRendererArgs(renderMatcher.group("args"));
                        Component modernComponent = parseModernRenderer(renderName, rendererArgs);
                        if (modernComponent != null) {
                            lineComponent.child(modernComponent);
                        } else {
                            IWailaTooltipRenderer renderer = ModuleRegistrar.instance().getTooltipRenderer(renderName);
                            if (renderer != null) {
                                lineComponent.child(new LegacyRendererComponent(renderer, rendererArgs));
                            }
                        }
                    } else if (iconMatcher.find()) {
                        String iconArg = iconMatcher.group("type");
                        lineComponent.child(iconParser.parse(new String[] { iconArg }));
                    } else {
                        lineComponent.text(DisplayUtil.stripWailaSymbols(cs));
                    }
                }
            }
        }
        return verticalLayout;
    }

    /** Selects the native WDMla implementation for every renderer built into current GTNH Waila and GregTech. */
    private Component parseModernRenderer(String renderName, String[] args) {
        return switch (renderName) {
            case "waila.health" -> healthParser.parse(args);
            case "waila.stack" -> itemParser.parse(args);
            case "waila.progress" -> progressParser.parse(args);
            case "waila.fluid" -> fluidParser.parse(args);
            case "waila.rfenergy" -> energyParser.parse(args);
            case "waila.tcaspect" -> aspectParser.parse(args);
            case "waila.gt.progress" -> gtProgressParser.parse(args);
            case "waila.cropsnh.cropStick.progress" -> cropsNHProgressParser.parse(args);
            default -> null;
        };
    }

    /** Splits current Waila renderer arguments while accepting the legacy comma encoding. */
    private static String[] splitRendererArgs(String args) {
        if (args.contains(WailaRendererComma)) {
            return args.split(Pattern.quote(WailaRendererComma), -1);
        }
        return args.split(",", -1);
    }
}
