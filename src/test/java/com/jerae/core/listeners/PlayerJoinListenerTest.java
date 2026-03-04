package com.jerae.core.listeners;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerJoinListenerTest {

    private ServerMock server;
    private CorePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testNicknameRestoredOnJoin() {
        PlayerMock player = server.addPlayer("JoinTestPlayer");

        NamespacedKey key = new NamespacedKey(plugin, "nickname");
        player.getPersistentDataContainer().set(key, PersistentDataType.STRING, "§aCustomName");

        // Simulating the execution of the player join listener explicitly
        com.jerae.core.listeners.PlayerJoinListener listener = new com.jerae.core.listeners.PlayerJoinListener(plugin);
        listener.onPlayerJoin(new org.bukkit.event.player.PlayerJoinEvent(player, net.kyori.adventure.text.Component.empty()));

        String displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        assertEquals("CustomName", displayName);
    }
}
