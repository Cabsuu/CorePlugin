package com.jerae.core.listeners;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerJoinListener implements Listener {

    private final CorePlugin plugin;

    public PlayerJoinListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NamespacedKey nicknameKey = new NamespacedKey(plugin, "nickname");

        if (player.getPersistentDataContainer().has(nicknameKey, PersistentDataType.STRING)) {
            String savedNickname = player.getPersistentDataContainer().get(nicknameKey, PersistentDataType.STRING);
            if (savedNickname != null) {
                Component displayComponent = LegacyComponentSerializer.legacySection().deserialize(savedNickname);
                player.displayName(displayComponent);
            }
        }
    }
}
