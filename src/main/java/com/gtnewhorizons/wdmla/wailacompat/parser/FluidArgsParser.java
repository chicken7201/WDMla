package com.gtnewhorizons.wdmla.wailacompat.parser;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.drawable.FluidDrawable;
import com.gtnewhorizons.wdmla.impl.ui.style.ProgressStyle;

import mcp.mobius.waila.cbcore.LangUtil;

public class FluidArgsParser implements ITTRenderParser {

    private static final String EMPTY_FLUID = "EMPTYFLUID";

    /** Converts Waila's fluid gauge arguments into a textured WDMla progress component. */
    @Override
    public Component parse(String[] args) {
        String fluidName = args[0];
        String localizedName = args[1];
        double amount = Double.parseDouble(args[2]);
        double capacity = Double.parseDouble(args[3]);
        boolean empty = EMPTY_FLUID.equals(fluidName) && EMPTY_FLUID.equals(localizedName);
        float ratio = progressRatio(empty ? 0.0 : amount, capacity);

        ProgressComponent progress = new ProgressComponent(ratio);
        if (!empty) {
            Fluid fluid = FluidRegistry.getFluid(fluidName);
            if (fluid != null) {
                progress.style(new ProgressStyle().overlay(new FluidDrawable(new FluidStack(fluid, 1000))));
            }
        }
        progress.child(
                new TextComponent(buildDisplayText(amount, localizedName, empty))
                        .padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING));
        return progress;
    }

    /** Formats a Waila fluid gauge as "fluid name: current amount". */
    private static String buildDisplayText(double amount, String fluidName, boolean empty) {
        String name = empty ? LangUtil.translateG("hud.msg.empty") : fluidName;
        return String.format("%s: %smB", name, formatNumber(empty ? 0.0 : amount));
    }

    /** Produces a finite progress ratio while matching Waila's over-capacity behavior. */
    private static float progressRatio(double amount, double capacity) {
        double denominator = Math.max(capacity, amount);
        if (!Double.isFinite(amount) || !Double.isFinite(denominator) || amount <= 0.0 || denominator <= 0.0) {
            return 0.0f;
        }
        return (float) Math.max(0.0, Math.min(amount / denominator, 1.0));
    }
}
