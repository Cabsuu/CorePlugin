package com.jerae.core.listeners;

import com.jerae.core.CorePlugin;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatListenerTest {

    private ServerMock server;
    private CorePlugin plugin;
    private ChatListener listener;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
        listener = new ChatListener();
        server.getPluginManager().registerEvents(listener, plugin);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testChatColorPermission() {
        Player player = server.addPlayer();
        player.addAttachment(plugin).setPermission("cp.chat.color", true);

        Component message = Component.text("&aHello");
        AsyncChatEvent event = new AsyncChatEvent(true, player, new HashSet<>(), (source, sourceDisplayName, msg, viewer) -> msg, message, message, null);

        listener.onChat(event);

        String legacy = LegacyComponentSerializer.legacySection().serialize(event.message());
        assertEquals("§aHello", legacy);
    }

    @Test
    void testNoPermission() {
        Player player = server.addPlayer();
        // No permission

        Component message = Component.text("&aHello");
        AsyncChatEvent event = new AsyncChatEvent(true, player, new HashSet<>(), (source, sourceDisplayName, msg, viewer) -> msg, message, message, null);

        listener.onChat(event);

        String legacy = LegacyComponentSerializer.legacySection().serialize(event.message());
        assertEquals("&aHello", legacy);
    }

    @Test
    void testGradientPermission() {
        Player player = server.addPlayer();
        player.addAttachment(plugin).setPermission("cp.chat.gradient", true);

        String input = "<#000000:#FFFFFF>Test";
        Component message = Component.text(input);
        AsyncChatEvent event = new AsyncChatEvent(true, player, new HashSet<>(), (source, sourceDisplayName, msg, viewer) -> msg, message, message, null);

        listener.onChat(event);

        String legacy = LegacyComponentSerializer.legacySection().serialize(event.message());
        assertNotEquals(input, legacy);
        // Should contain color codes (legacy format usually starts with §)
        // Note: serializer might downsample to nearest legacy color in test environment, so we check for §
        assertTrue(legacy.contains("§"));
    }
}
