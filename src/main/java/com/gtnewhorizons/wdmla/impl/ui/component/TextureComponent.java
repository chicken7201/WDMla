package com.gtnewhorizons.wdmla.impl.ui.component;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.impl.ui.drawable.TextureDrawable;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Size;

/** A modern component for standalone textures that do not belong to an icon atlas. */
public class TextureComponent extends Component {

    /** Creates a tinted texture component at the requested logical size. */
    public TextureComponent(ResourceLocation texture, int color, float width, float height) {
        super(new Padding(), new Size(width, height), new TextureDrawable(texture, color));
    }
}
