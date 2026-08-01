package mcp.mobius.waila.overlay.tooltiprenderers;

import java.awt.Dimension;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.registry.GameData;
import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.overlay.DisplayUtil;

public class TTRenderStack implements IWailaTooltipRenderer {

    /** Returns the display area reserved for a normal or small item stack. */
    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        boolean small = Boolean.parseBoolean(params.length > 4 ? params[4] : "false");
        return small ? new Dimension(10, 8) : new Dimension(16, 16);
    }

    /** Draws the encoded block or item stack in the reserved renderer area. */
    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        int type = Integer.parseInt(params[0]);
        String name = params[1];
        int amount = Integer.parseInt(params[2]);
        int meta = Integer.parseInt(params[3]);
        boolean small = Boolean.parseBoolean(params.length > 4 ? params[4] : "false");
        final float scale = 0.5f;

        ItemStack stack = null;
        if (type == 0) stack = new ItemStack((Block) Block.blockRegistry.getObject(name), amount, meta);
        if (type == 1) stack = new ItemStack((Item) Item.itemRegistry.getObject(name), amount, meta);

        RenderHelper.enableGUIStandardItemLighting();

        if (small) {
            GL11.glPushMatrix();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glScaled(scale, scale, 1.0f);
            DisplayUtil.renderStack(0, 0, stack);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
        } else {
            DisplayUtil.renderStack(0, 0, stack);
        }
        RenderHelper.disableStandardItemLighting();
    }

    /** Encodes an item stack renderer token with an explicit amount. */
    public static String create(ItemStack itemStack, int amount, boolean small) {
        String name = GameData.getItemRegistry().getNameForObject(itemStack.getItem());
        int meta = itemStack.getItemDamage();
        return SpecialChars.getRenderString(
                "waila.stack",
                String.valueOf(1),
                name,
                String.valueOf(amount),
                String.valueOf(meta),
                String.valueOf(small));
    }

    /** Encodes a single-item stack renderer token. */
    public static String create(ItemStack itemStack, boolean small) {
        return create(itemStack, 1, small);
    }
}
