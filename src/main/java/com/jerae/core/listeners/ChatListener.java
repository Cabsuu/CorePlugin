package com.jerae.core.listeners;

import com.jerae.core.utils.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.jerae.core.CorePlugin;
import com.jerae.core.commands.ChatColorCommand;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

public class ChatListener implements Listener {

    private final CorePlugin plugin;

    public ChatListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component message = event.message();

        // Convert the component to plain text
        String content = PlainTextComponentSerializer.plainText().serialize(message);

        // Check permissions
        boolean color = player.hasPermission("cp.chat.color");
        boolean format = player.hasPermission("cp.chat.format");
        boolean rgb = player.hasPermission("cp.chat.rgb");
        boolean gradient = player.hasPermission("cp.chat.gradient");

        NamespacedKey key = new NamespacedKey(plugin, "chat_color");
        String chatColorSetting = player.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        String translatedPrefix = "";

        if (chatColorSetting != null) {
            if (chatColorSetting.startsWith("type:color,value:")) {
                String colorName = chatColorSetting.split(":")[2];
                if (colorName.equals("white") || player.hasPermission("cp.chatcolor." + colorName)) {
                    String prefixColor = ChatColorCommand.COLOR_CODES.getOrDefault(colorName, "&f");
                    translatedPrefix = ColorUtil.translate(prefixColor, true, false, false, false);
                }
            } else if (chatColorSetting.startsWith("type:rgb,value:")) {
                if (player.hasPermission("cp.chatcolor.rgb")) {
                    String hex = chatColorSetting.split(":")[2];
                    String prefixColor = "&x" + hex.replace("#", "");
                    translatedPrefix = ColorUtil.translate(prefixColor, false, false, true, false);
                }
            } else if (chatColorSetting.startsWith("type:gradient,value:")) {
                if (player.hasPermission("cp.chatcolor.gradient")) {
                    String[] parts = chatColorSetting.substring("type:gradient,value:".length()).split(":");
                    if (parts.length == 2) {
                        // For gradient to work properly across the whole string, ColorUtil needs the whole string.
                        // However, since gradient is an interpolator, appending the plain content after translation
                        // won't apply the gradient. We MUST pass the content with the gradient prefix into ColorUtil.
                        // Wait, gradient requires the `<color1:color2>Content` syntax.
                        String prefixColor = "<" + parts[0] + ":" + parts[1] + ">";
                        // If we translate just the prefix, we get an empty string. Gradient only works on text immediately following it until it stops.
                        // So for gradient, we MUST prepend it to content. But if we prepend to content and use gradient=true,
                        // we'd be granting gradient to their own tags too. But that's acceptable if they have cp.chatcolor.gradient?
                        // Actually, if they have cp.chatcolor.gradient, they inherently have permission to use gradients in chat.
                        // If they don't, we wouldn't set the gradient here anyway.
                        // Wait, for color and rgb, they only selected a color, they shouldn't automatically get the permission to type ANY color code.
                    }
                }
            }
        }

        // To safely handle prefixes without granting global chat color permissions, we must:
        // 1. Translate the user's content using their actual permissions.
        // 2. Prepend the raw prefix to the RAW content IF the prefix is a gradient (so ColorUtil interpolates over the text).
        // Let's refactor:

        String finalContent = content;
        boolean forceColor = false;
        boolean forceRgb = false;
        boolean forceGradient = false;
        String prefix = "";

        if (chatColorSetting != null) {
            if (chatColorSetting.startsWith("type:color,value:")) {
                String colorName = chatColorSetting.split(":")[2];
                if (colorName.equals("white") || player.hasPermission("cp.chatcolor." + colorName)) {
                    prefix = ChatColorCommand.COLOR_CODES.getOrDefault(colorName, "&f");
                    forceColor = true;
                }
            } else if (chatColorSetting.startsWith("type:rgb,value:")) {
                if (player.hasPermission("cp.chatcolor.rgb")) {
                    String hex = chatColorSetting.split(":")[2];
                    prefix = "&x" + hex.replace("#", "");
                    forceRgb = true;
                }
            } else if (chatColorSetting.startsWith("type:gradient,value:")) {
                if (player.hasPermission("cp.chatcolor.gradient")) {
                    String[] parts = chatColorSetting.substring("type:gradient,value:".length()).split(":");
                    if (parts.length == 2) {
                        prefix = "<" + parts[0] + ":" + parts[1] + ">";
                        forceGradient = true;
                    }
                }
            }
        }

        // Translate the user's content first if they have permissions
        String translatedContent = content;
        if (color || format || rgb || gradient) {
            translatedContent = ColorUtil.translate(content, color, format, rgb, gradient);
        }

        // Now prepend our prefix and translate ONLY the prefix feature type, without translating embedded user codes again.
        // Because translatedContent already contains § or is plain. If we pass it back into ColorUtil.translate with forceColor=true,
        // it will translate ANY un-translated &a tags the user put. We DO NOT want that.
        // Wait, standard ColorUtil.translate replaces &a with §a. If forceColor=true, it will translate &a in the user's message!
        // So we can translate the prefix completely standalone for simple colors:
        String fullTranslated = translatedContent;
        if (!prefix.isEmpty()) {
            if (forceGradient) {
                // Gradient NEEDS the text. So we must prepend the gradient tag to the translated content,
                // and then run gradient translation over it.
                // Since the user might have used & tags (which are now §), ColorUtil handles § resetting gradient.
                fullTranslated = ColorUtil.translate(prefix + translatedContent, false, false, false, true);
            } else if (forceRgb) {
                // RGB can be translated standalone because it's just a hex code replacement
                translatedPrefix = ColorUtil.translate(prefix, false, false, true, false);
                fullTranslated = translatedPrefix + translatedContent;
            } else if (forceColor) {
                translatedPrefix = ColorUtil.translate(prefix, true, false, false, false);
                fullTranslated = translatedPrefix + translatedContent;
            }
        }

        if (fullTranslated != null && !fullTranslated.equals(content)) {
            // Convert back to Component using legacy section serializer (handles § codes)
            Component newMessage = LegacyComponentSerializer.legacySection().deserialize(fullTranslated);
            event.message(newMessage);
        }
    }
}
