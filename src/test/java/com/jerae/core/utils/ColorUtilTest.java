package com.jerae.core.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorUtilTest {

    @Test
    void testStandardColors() {
        String input = "&aHello";
        String expected = "§aHello";
        assertEquals(expected, ColorUtil.translate(input, true, false, false, false));
    }

    @Test
    void testStandardFormats() {
        String input = "&lBold";
        String expected = "§lBold";
        assertEquals(expected, ColorUtil.translate(input, false, true, false, false));
    }

    @Test
    void testRGB() {
        String input = "&x#FFFFFF";
        // Check if it converts to legacy hex
        String result = ColorUtil.translate(input, false, false, true, false);
        // RGB #FFFFFF is white, legacy is §x§f§f§f§f§f§f
        // Note: ChatColor might use lowercase or uppercase depending on implementation, usually lowercase.
        assertTrue(result.toLowerCase().contains("§x"));
        assertTrue(result.toLowerCase().contains("§f"));
    }

    @Test
    void testGradient() {
        String input = "<#000000:#FFFFFF>Test";
        String result = ColorUtil.translate(input, false, false, false, true);
        // Should contain colored characters
        // T, e, s, t should each be preceded by §x...
        assertTrue(result.contains("§x"));
        assertTrue(result.contains("T"));
        assertTrue(result.contains("e"));
        assertTrue(result.contains("s"));
        assertTrue(result.contains("t"));
    }

    @Test
    void testGradientWithStandardColors() {
        String input = "<&0:&f>Test";
        String result = ColorUtil.translate(input, false, false, false, true);
        assertTrue(result.contains("§x"));
    }

    @Test
    void testPermissionsDisabled() {
        String input = "&aHello";
        assertEquals(input, ColorUtil.translate(input, false, false, false, false));

        input = "&x#FFFFFF";
        assertEquals(input, ColorUtil.translate(input, false, false, false, false));

        input = "<#000000:#FFFFFF>Test";
        assertEquals(input, ColorUtil.translate(input, false, false, false, false));
    }

    @Test
    void testComplexMix() {
        String input = "&aHello <#000000:#FFFFFF>World &b!";
        String result = ColorUtil.translate(input, true, true, true, true);

        assertTrue(result.startsWith("§aHello "));
        assertTrue(result.endsWith(" §b!"));
        // Middle part should be hex codes
        // Check that "World" isn't plain text
        assertTrue(!result.contains("World"));
        // Instead it is W...o...r...l...d...
        assertTrue(result.contains("W") && result.contains("o") && result.contains("r") && result.contains("l") && result.contains("d"));
    }

    @Test
    void testGradientWithFormatBefore() {
        // &l<#000000:#FFFFFF>Test
        // Should produce bold characters throughout the gradient
        String input = "&l<#000000:#FFFFFF>Test";
        String result = ColorUtil.translate(input, false, false, false, true);

        // Check for §l
        assertTrue(result.contains("§l"));
        // Since hex colors reset formatting, §l should appear multiple times (re-applied)
        // Check if §l appears after a hex code sequence
        // Hex sequence is §x§.§.§.§.§.§.
        // So we expect §x...§lT...
        assertTrue(result.contains("§lT"));
    }

    @Test
    void testGradientWithFormatInside() {
        // <#000000:#FFFFFF>&lTest
        // The parser should include &l in the gradient processing
        String input = "<#000000:#FFFFFF>&lTest";
        String result = ColorUtil.translate(input, false, false, false, true);

        // &l should be applied
        assertTrue(result.contains("§l"));
        assertTrue(result.contains("§lT"));
    }
}
