package com.jerae.core.listeners;

import com.jerae.core.utils.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

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

        // If no permissions, do nothing (or translate only what is allowed, which might be nothing)
        if (!color && !format && !rgb && !gradient) {
            return;
        }

        String translated = ColorUtil.translate(content, color, format, rgb, gradient);

        if (translated != null && !translated.equals(content)) {
            // Convert back to Component using legacy section serializer (handles § codes)
            Component newMessage = LegacyComponentSerializer.legacySection().deserialize(translated);
            event.message(newMessage);
        }
    }
}
