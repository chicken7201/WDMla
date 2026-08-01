package mcp.mobius.waila.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.wdmla.gui.ModsMenuScreenConfig;

import cpw.mods.fml.client.IModGuiFactory;

/** Provides the current Waila GUI factory class name for binary compatibility. */
public class WailaGuiFactory implements IModGuiFactory {

    /** Initializes the compatibility GUI factory. */
    @Override
    public void initialize(Minecraft minecraftInstance) {}

    /** Returns WDMla's Waila-compatible main configuration screen. */
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ModsMenuScreenConfig.class;
    }

    /** Returns the optional runtime GUI categories. */
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    /** Returns the handler for an optional runtime GUI category. */
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
