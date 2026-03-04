package com.jerae.core.utils;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AFKManagerTest {

    private ServerMock server;
    private CorePlugin plugin;
    private Messages messages;
    private AFKManager afkManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
        messages = new Messages(plugin);
        afkManager = new AFKManager(plugin, messages);
    }

    @AfterEach
    void tearDown() {
        afkManager.stopTask();
        MockBukkit.unmock();
    }

    @Test
    void testToggleAfkUpdatesPlayerListName() {
        PlayerMock player = server.addPlayer("AfkPlayer");
        String originalName = PlainTextComponentSerializer.plainText().serialize(player.playerListName());

        afkManager.setAfk(player, true, null);
        assertTrue(afkManager.isAfk(player));

        String newName = PlainTextComponentSerializer.plainText().serialize(player.playerListName());
        assertEquals(originalName + " AFK", newName);

        afkManager.setAfk(player, false, null);
        assertFalse(afkManager.isAfk(player));

        String restoredName = PlainTextComponentSerializer.plainText().serialize(player.playerListName());
        assertEquals(originalName, restoredName);
    }
}
