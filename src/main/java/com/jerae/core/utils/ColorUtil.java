package com.jerae.core.utils;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<([0-9A-Fa-f]{6}|&[0-9a-fA-Fk-or]|#[0-9A-Fa-f]{6})(?::([0-9A-Fa-f]{6}|&[0-9a-fA-Fk-or]|#[0-9A-Fa-f]{6}))+>");
    private static final Pattern RGB_PATTERN = Pattern.compile("&x#([0-9A-Fa-f]{6})");

    public static String translate(String message, boolean color, boolean format, boolean rgb, boolean gradient) {
        if (message == null) return null;

        if (gradient) {
            Matcher matcher = GRADIENT_PATTERN.matcher(message);
            while (matcher.find()) {
                String fullMatch = matcher.group();
                String content = fullMatch.substring(1, fullMatch.length() - 1);
                String[] colorCodes = content.split(":");

                // Find the text to color
                int start = matcher.end();
                int end = message.length();

                // Search for next color code or end
                // We look for & or < (start of next tag)
                // Note: We need to handle escaped & or similar if necessary, but simple scan is usually enough for chat
                int nextAmp = message.indexOf('&', start);
                int nextTag = message.indexOf('<', start);

                if (nextAmp != -1 && (nextTag == -1 || nextAmp < nextTag)) {
                    end = nextAmp;
                } else if (nextTag != -1) {
                    end = nextTag;
                }

                String text = message.substring(start, end);
                String coloredText = applyGradient(text, colorCodes);

                // Replace the tag AND the text with the colored text
                // Be careful with indices since we change the string length
                message = message.substring(0, matcher.start()) + coloredText + message.substring(end);

                // Reset matcher because string changed
                matcher = GRADIENT_PATTERN.matcher(message);
            }
        }

        if (rgb) {
            Matcher matcher = RGB_PATTERN.matcher(message);
            while (matcher.find()) {
                String hex = matcher.group(1);
                // ChatColor.of works with #RRGGBB
                String replacement = ChatColor.of("#" + hex).toString();
                message = matcher.replaceFirst(replacement);
                matcher = RGB_PATTERN.matcher(message);
            }
        }

        if (color) {
            for (char c : "0123456789abcdef".toCharArray()) {
                message = message.replace("&" + c, "§" + c);
                message = message.replace("&" + Character.toUpperCase(c), "§" + c);
            }
        }

        if (format) {
            for (char c : "klmnor".toCharArray()) {
                message = message.replace("&" + c, "§" + c);
                message = message.replace("&" + Character.toUpperCase(c), "§" + c);
            }
        }

        return message;
    }

    private static String applyGradient(String text, String[] colorCodes) {
        if (text.isEmpty()) return "";

        List<Color> colors = new ArrayList<>();
        for (String code : colorCodes) {
            if (code.startsWith("&")) {
                ChatColor cc = ChatColor.getByChar(code.charAt(1));
                if (cc != null && cc.getColor() != null) {
                     colors.add(cc.getColor());
                } else {
                     // Fallback to white if invalid code
                     colors.add(Color.WHITE);
                }
            } else if (code.startsWith("#")) {
                colors.add(Color.decode(code));
            } else {
                // assume hex without #
                colors.add(Color.decode("#" + code));
            }
        }

        StringBuilder sb = new StringBuilder();
        int steps = text.length();
        int colorSteps = colors.size() - 1;

        if (colorSteps < 1) return text;

        for (int i = 0; i < text.length(); i++) {
            float ratio = (float) i / (float) (text.length() - 1);
            if (text.length() == 1) ratio = 0;

            // Find which segment
            float segmentPos = ratio * colorSteps;
            int segmentIndex = (int) segmentPos;
            if (segmentIndex >= colorSteps) segmentIndex = colorSteps - 1;

            float segmentRatio = segmentPos - segmentIndex;

            Color c1 = colors.get(segmentIndex);
            Color c2 = colors.get(segmentIndex + 1);

            Color c = interpolate(c1, c2, segmentRatio);

            sb.append(ChatColor.of(c));
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }

    private static Color interpolate(Color c1, Color c2, float ratio) {
        int red = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int green = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int blue = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        return new Color(red, green, blue);
    }
}
