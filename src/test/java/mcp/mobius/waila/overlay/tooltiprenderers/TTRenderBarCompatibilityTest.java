package mcp.mobius.waila.overlay.tooltiprenderers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import mcp.mobius.waila.api.SpecialChars;

class TTRenderBarCompatibilityTest {

    /** Verifies the four-argument ABI used by GregTech creates a decodable Waila bar token. */
    @Test
    void createsGregTechCompatibleBarToken() {
        String encoded = TTRenderBar.create("Stored EU", 0xFFFFEE55, 0xFFFFA500, 0.25);
        Matcher matcher = SpecialChars.patternRender.matcher(encoded);

        assertTrue(matcher.matches());
        assertEquals("waila.bar", matcher.group("name"));
        assertArrayEquals(
                new String[] { "Stored EU", "-4523", "-23296", "0.25" },
                matcher.group("args").split(Pattern.quote(SpecialChars.WailaRendererComma), -1));
    }

    /** Verifies the single-colour overload duplicates its colour for both gradient endpoints. */
    @Test
    void createsSingleColorBarToken() {
        String encoded = TTRenderBar.create("Progress", 0xFF00AA00, 0.5);
        Matcher matcher = SpecialChars.patternRender.matcher(encoded);

        assertTrue(matcher.matches());
        assertArrayEquals(
                new String[] { "Progress", "-16733696", "-16733696", "0.5" },
                matcher.group("args").split(Pattern.quote(SpecialChars.WailaRendererComma), -1));
    }
}
