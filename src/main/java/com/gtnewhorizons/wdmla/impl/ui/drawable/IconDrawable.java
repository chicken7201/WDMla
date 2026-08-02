package com.gtnewhorizons.wdmla.impl.ui.drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.ui.IDrawable;
import com.gtnewhorizons.wdmla.api.ui.IFilledProgress;
import com.gtnewhorizons.wdmla.api.ui.sizer.IArea;
import com.gtnewhorizons.wdmla.overlay.GuiDraw;

public class IconDrawable implements IDrawable {

    private final IIcon icon;
    private final ResourceLocation path;
    private float suRatio = 0, svRatio = 0, twRatio = 1, thRatio = 1;
    private IFilledProgress horizontalProgress;

    public IconDrawable(IIcon icon, ResourceLocation path) {
        this.icon = icon;
        this.path = path;
    }

    public void clip(float suRatio, float svRatio, float twRatio, float thRatio) {
        this.suRatio = suRatio;
        this.svRatio = svRatio;
        this.twRatio = twRatio;
        this.thRatio = thRatio;
    }

    /** Uses an animated progress value as the horizontal clipping width. */
    public void clipProgress(IFilledProgress progress) {
        horizontalProgress = progress;
    }

    @Override
    public void draw(IArea area) {
        if (icon == null) {
            return;
        }

        // we must shrink the texture uv very slightly
        // to fill gui scale and uv scale difference
        final float epsilon = 0.1f / 256.0f;

        float currentTwRatio = twRatio;
        if (horizontalProgress != null && horizontalProgress.getMax() > 0L) {
            currentTwRatio = (float) horizontalProgress.getCurrent() / horizontalProgress.getMax();
        }

        float x = area.getX() + area.getW() * suRatio;
        float y = area.getY() + area.getH() * svRatio;
        float w = area.getW() * currentTwRatio;
        float h = area.getH() * thRatio;

        float fullU = icon.getMaxU() - epsilon - icon.getMinU();
        float fullV = icon.getMaxV() - epsilon - icon.getMinV();

        float u0 = icon.getMinU() + epsilon + fullU * suRatio;
        float v0 = icon.getMinV() + epsilon + fullV * svRatio;

        float tw = fullU * w / area.getW();
        float th = fullV * h / area.getH();

        Minecraft.getMinecraft().getTextureManager().bindTexture(path);
        GuiDraw.drawTexturedModelRect(x, y, u0, v0, w, h, tw, th);
    }
}
