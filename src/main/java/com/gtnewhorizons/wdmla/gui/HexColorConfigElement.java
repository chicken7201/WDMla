package com.gtnewhorizons.wdmla.gui;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizons.wdmla.util.ConfigColorCodec;

import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.IConfigElement;

/** Presents an integer color config element as validated hexadecimal text. */
public final class HexColorConfigElement extends DummyConfigElement<String> {

    private static final String TEXT_COLOR_PREFIX = "option.wdmla.general.textcolor.";
    private static final String PROGRESS_COLOR_PREFIX = "option.wdmla.general.progresscolor.";

    private final IConfigElement<Integer> delegate;

    /** Creates a hexadecimal GUI view backed by the original integer element. */
    private HexColorConfigElement(IConfigElement<Integer> delegate) {
        super(
                delegate.getName(),
                ConfigColorCodec.format((Integer) delegate.get()),
                ConfigGuiType.STRING,
                delegate.getLanguageKey(),
                ConfigColorCodec.INPUT_PATTERN);
        this.delegate = delegate;
    }

    /** Wraps WDMla text and progress color properties in a hexadecimal editor. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<IConfigElement> wrapColorElements(List<IConfigElement> elements) {
        List<IConfigElement> wrapped = new ArrayList<>(elements.size());
        for (IConfigElement element : elements) {
            wrapped.add(isColorElement(element) ? new HexColorConfigElement((IConfigElement<Integer>) element) : element);
        }
        return wrapped;
    }

    /** Identifies integer properties belonging to WDMla's configurable color groups. */
    @SuppressWarnings("rawtypes")
    private static boolean isColorElement(IConfigElement element) {
        String languageKey = element.getLanguageKey();
        return element.isProperty() && element.getType() == ConfigGuiType.INTEGER
                && languageKey != null
                && (languageKey.startsWith(TEXT_COLOR_PREFIX) || languageKey.startsWith(PROGRESS_COLOR_PREFIX));
    }

    /** Returns the original fully qualified config path. */
    @Override
    public String getQualifiedName() {
        return delegate.getQualifiedName();
    }

    /** Returns the original field comment. */
    @Override
    public String getComment() {
        return delegate.getComment();
    }

    /** Reports whether the backing integer still equals its default. */
    @Override
    public boolean isDefault() {
        return delegate.isDefault();
    }

    /** Returns the original default converted to hexadecimal GUI text. */
    @Override
    public Object getDefault() {
        return ConfigColorCodec.format((Integer) delegate.getDefault());
    }

    /** Restores the integer default and refreshes the displayed hexadecimal value. */
    @Override
    public void setToDefault() {
        delegate.set((Integer) delegate.getDefault());
        value = ConfigColorCodec.format((Integer) delegate.get());
    }

    /** Preserves the original world-restart requirement. */
    @Override
    public boolean requiresWorldRestart() {
        return delegate.requiresWorldRestart();
    }

    /** Preserves the original GUI visibility. */
    @Override
    public boolean showInGui() {
        return delegate.showInGui();
    }

    /** Preserves the original Minecraft-restart requirement. */
    @Override
    public boolean requiresMcRestart() {
        return delegate.requiresMcRestart();
    }

    /** Saves valid hexadecimal text as an integer ARGB value and ignores invalid text. */
    @Override
    public void set(String input) {
        Integer parsed = ConfigColorCodec.parse(input);
        if (parsed != null) {
            delegate.set(parsed);
            value = ConfigColorCodec.format(parsed);
        }
    }
}
