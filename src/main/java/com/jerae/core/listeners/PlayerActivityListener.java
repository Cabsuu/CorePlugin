package com.jerae.core.listeners;

import com.jerae.core.utils.AFKManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerActivityListener implements Listener {

    private final AFKManager afkManager;

    public PlayerActivityListener(AFKManager afkManager) {
        this.afkManager = afkManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock() || event.hasChangedOrientation()) {
            afkManager.updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        afkManager.updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        afkManager.updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        afkManager.playerQuit(event.getPlayer());
    }
}
