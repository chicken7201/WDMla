package com.gtnewhorizons.wdmla.wailacompat.parser;

import java.awt.Color;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextureComponent;
import com.gtnewhorizons.wdmla.impl.ui.style.PanelStyle;

import mcp.mobius.waila.addons.thaumcraft.ThaumcraftModule;

public class AspectArgsParser implements ITTRenderParser {

    private static final ResourceLocation UNKNOWN_TEXTURE = new ResourceLocation(
            "thaumcraft",
            "textures/aspects/_unknown.png");
    private static final int UNKNOWN_COLOR = new Color(0x654242).getRGB();

    /** Converts a Thaumcraft aspect tag into its tinted icon and localized aspect name. */
    @Override
    public Component parse(String[] args) {
        String tag = args[0];
        if ("???".equals(tag)) {
            return buildAspect(UNKNOWN_TEXTURE, UNKNOWN_COLOR, tag);
        }
        try {
            if (ThaumcraftModule.Aspect_getAspect == null) {
                return new TextComponent(tag);
            }
            Object aspect = ThaumcraftModule.Aspect_getAspect.invoke(null, tag);
            if (aspect == null) {
                return new TextComponent(tag);
            }
            ResourceLocation image = (ResourceLocation) ThaumcraftModule.Aspect_getImage.invoke(aspect);
            int color = (Integer) ThaumcraftModule.Aspect_getColor.invoke(aspect);
            String name = (String) ThaumcraftModule.Aspect_getName.invoke(aspect);
            return buildAspect(image, color, name);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return new TextComponent(tag);
        }
    }

    /** Builds the consistently spaced modern icon-and-label row for an aspect. */
    private static Component buildAspect(ResourceLocation image, int color, String name) {
        HPanelComponent result = new HPanelComponent();
        result.style(new PanelStyle().spacing(2));
        result.child(new TextureComponent(image, color, 8, 8));
        result.child(new TextComponent(name));
        return result;
    }
}
