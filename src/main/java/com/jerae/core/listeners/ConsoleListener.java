package com.jerae.core.listeners;

import com.jerae.core.utils.ColorUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

public class ConsoleListener implements Listener {

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        // Check if command is "say"
        if (command.toLowerCase().startsWith("say ")) {
            String content = command.substring(4);
            // Console has all permissions implicitly or we just allow all features for console
            String translated = ColorUtil.translate(content, true, true, true, true);
            event.setCommand("say " + translated);
        }
    }
}
