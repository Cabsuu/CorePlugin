package com.jerae.core.utils;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<([0-9A-Fa-f]{6}|&[0-9a-fA-Fk-or]|#[0-9A-Fa-f]{6})(?::([0-9A-Fa-f]{6}|&[0-9a-fA-Fk-or]|#[0-9A-Fa-f]{6}))+>");
    private static final Pattern RGB_PATTERN = Pattern.compile("&x#([0-9A-Fa-f]{6})");
    private static final Pattern FORMAT_PATTERN = Pattern.compile("&[k-oK-OrR]");

    public static String translate(String message, boolean color, boolean format, boolean rgb, boolean gradient) {
        if (message == null) return null;

        if (gradient) {
            Matcher matcher = GRADIENT_PATTERN.matcher(message);
            while (matcher.find()) {
                String fullMatch = matcher.group();
                String content = fullMatch.substring(1, fullMatch.length() - 1);
                String[] colorCodes = content.split(":");

                // Find initial formats (look behind)
                String prefix = message.substring(0, matcher.start());
                Set<Character> activeFormats = new HashSet<>();
                // Scan backwards for format codes
                // This is a simplified approach: we look for contiguous format codes immediately preceding the tag
                // Example: "&l&n<...>"
                // We stop at the first non-format character (or end of string going backwards)
                // Note: We also need to handle standard color codes which would reset formats?
                // Actually, standard color codes reset formats. So we only care about formats *after* the last color code.
                // For now, let's just grab all formats at the end of the prefix string.

                int i = prefix.length() - 1;
                while (i >= 1) {
                    char c = prefix.charAt(i);
                    char prev = prefix.charAt(i - 1);
                    if (prev == '&' || prev == '§') { // Allow both just in case, though usually input has &
                        if ("klmnorKLMNOR".indexOf(c) != -1) {
                            if (c == 'r' || c == 'R') {
                                activeFormats.clear();
                            } else {
                                activeFormats.add(Character.toLowerCase(c));
                            }
                            i -= 2; // Skip code and char
                            continue;
                        } else if ("0123456789abcdefABCDEF".indexOf(c) != -1) {
                            // Color code resets formats
                            activeFormats.clear();
                            break;
                        }
                    }
                    break;
                }

                // Find the text to color
                int start = matcher.end();
                int end = message.length();

                // Special handling: if nextAmp points to a format code, we should include it in the gradient text!
                // The original logic stopped at ANY '&'.
                // New logic: Stop at '&' ONLY IF it is followed by a color code (0-9, a-f) or 'r'.
                // Formats (k-o) should be part of the text and applied.
                // However, 'nextTag' (<) definitely stops it.

                // Let's refine the scan:
                int current = start;
                while (current < message.length()) {
                    if (message.charAt(current) == '<') {
                        // Check if it's a gradient start? Or just a random < ?
                        // Assuming any < might be a tag start, we stop.
                        // Ideally we check if it matches pattern, but simple check is safer/faster
                        end = current;
                        break;
                    }
                    if (message.charAt(current) == '&' && current + 1 < message.length()) {
                        char code = message.charAt(current + 1);
                        if ("0123456789abcdefABCDEFxX".indexOf(code) != -1) {
                            // Color code or RGB start -> Stop
                            end = current;
                            break;
                        }
                        // If it's a format code (k-o), we continue!
                    }
                    current++;
                }

                String text = message.substring(start, end);
                String coloredText = applyGradient(text, colorCodes, activeFormats);

                message = message.substring(0, matcher.start()) + coloredText + message.substring(end);

                matcher = GRADIENT_PATTERN.matcher(message);
            }
        }

        if (rgb) {
            Matcher matcher = RGB_PATTERN.matcher(message);
            while (matcher.find()) {
                String hex = matcher.group(1);
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

    private static String applyGradient(String text, String[] colorCodes, Set<Character> initialFormats) {
        if (text.isEmpty()) return "";

        List<Color> colors = new ArrayList<>();
        for (String code : colorCodes) {
            if (code.startsWith("&")) {
                ChatColor cc = ChatColor.getByChar(code.charAt(1));
                if (cc != null && cc.getColor() != null) {
                     colors.add(cc.getColor());
                } else {
                     colors.add(Color.WHITE);
                }
            } else if (code.startsWith("#")) {
                colors.add(Color.decode(code));
            } else {
                colors.add(Color.decode("#" + code));
            }
        }

        StringBuilder sb = new StringBuilder();

        // We need to calculate the "effective length" of text (ignoring codes) for interpolation
        int effectiveLength = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&' && i + 1 < text.length()) {
                char code = text.charAt(i+1);
                 if ("0123456789abcdefABCDEFklmnorKLMNORxX".indexOf(code) != -1) {
                     i++;
                     continue;
                 }
            }
            effectiveLength++;
        }

        int colorSteps = colors.size() - 1;
        if (colorSteps < 1) return text;

        Set<Character> currentFormats = new HashSet<>(initialFormats);
        int currentEffectiveIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Handle codes in text
            if (c == '&' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if ("klmnoKLMNO".indexOf(code) != -1) {
                    currentFormats.add(Character.toLowerCase(code));
                    i++;
                    continue;
                } else if (code == 'r' || code == 'R') {
                    currentFormats.clear();
                    // Restore initial formats? Usually reset clears everything.
                    // But if it's a gradient, maybe we want to clear internal formats only?
                    // Standard behavior is clear all.
                    i++;
                    continue;
                }
                // If it's a color code, it shouldn't happen based on our parsing logic in translate()
                // But if it does, it would technically reset formats too.
            }

            float ratio = (float) currentEffectiveIndex / (float) (effectiveLength - 1);
            if (effectiveLength <= 1) ratio = 0;

            float segmentPos = ratio * colorSteps;
            int segmentIndex = (int) segmentPos;
            if (segmentIndex >= colorSteps) segmentIndex = colorSteps - 1;

            float segmentRatio = segmentPos - segmentIndex;

            Color c1 = colors.get(segmentIndex);
            Color c2 = colors.get(segmentIndex + 1);

            Color color = interpolate(c1, c2, segmentRatio);

            sb.append(ChatColor.of(color));

            // Re-apply formats
            for (Character format : currentFormats) {
                sb.append("§").append(format);
            }

            sb.append(c);
            currentEffectiveIndex++;
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
