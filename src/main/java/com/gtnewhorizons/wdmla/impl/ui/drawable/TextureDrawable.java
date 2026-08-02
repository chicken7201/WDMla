package com.gtnewhorizons.wdmla.impl.ui.drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.wdmla.api.ui.IDrawable;
import com.gtnewhorizons.wdmla.api.ui.sizer.IArea;
import com.gtnewhorizons.wdmla.overlay.GuiDraw;

/** Draws a complete standalone texture with an optional ARGB tint. */
public class TextureDrawable implements IDrawable {

    private final ResourceLocation texture;
    private final int color;

    /** Stores the texture and normalizes RGB-only colors to opaque ARGB. */
    public TextureDrawable(ResourceLocation texture, int color) {
        this.texture = texture;
        this.color = (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    /** Renders the whole texture into the requested component area and restores the GL tint. */
    @Override
    public void draw(IArea area) {
        float alpha = (color >>> 24 & 0xFF) / 255.0f;
        float red = (color >>> 16 & 0xFF) / 255.0f;
        float green = (color >>> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glColor4f(red, green, blue, alpha);
        try {
            GuiDraw.drawTexturedModelRect(
                    area.getX(),
                    area.getY(),
                    0.0f,
                    0.0f,
                    area.getW(),
                    area.getH(),
                    1.0f,
                    1.0f);
        } finally {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
