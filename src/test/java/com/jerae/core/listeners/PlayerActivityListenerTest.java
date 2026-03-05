package com.jerae.core.listeners;

import com.jerae.core.CorePlugin;
import com.jerae.core.utils.AFKManager;
import com.jerae.core.utils.Messages;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerActivityListenerTest {

    private ServerMock server;
    private CorePlugin plugin;
    private PlayerMock player;
    private AFKManager afkManager;
    private PlayerActivityListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
        Messages messages = new Messages(plugin);
        afkManager = new AFKManager(plugin, messages);
        listener = new PlayerActivityListener(afkManager);
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        afkManager.stopTask();
        MockBukkit.unmock();
    }

    @Test
    void testChatRemovesAfk() {
        afkManager.setAfk(player, true, null);
        assertTrue(afkManager.isAfk(player));

        AsyncChatEvent event = new AsyncChatEvent(true, player, Collections.emptySet(), null, Component.empty(), Component.empty(), null);
        listener.onPlayerChat(event);

        assertFalse(afkManager.isAfk(player));
    }

    @Test
    void testMoveRemovesAfk() {
        afkManager.setAfk(player, true, null);
        assertTrue(afkManager.isAfk(player));

        Location from = new Location(server.getWorld("world"), 0, 0, 0);
        Location to = new Location(server.getWorld("world"), 1, 0, 0);
        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
        listener.onPlayerMove(event);

        assertFalse(afkManager.isAfk(player));
    }
}
