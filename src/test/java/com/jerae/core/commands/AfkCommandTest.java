package com.jerae.core.commands;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AfkCommandTest {

    private ServerMock server;
    private CorePlugin plugin;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
        player = server.addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testNoPermission() {
        player.performCommand("afk");
        String serialized = LegacyComponentSerializer.legacySection().serialize(player.nextComponentMessage());
        assertEquals("§cYou do not have permission to use this command.", serialized);
    }

    @Test
    void testToggleAfk() {
        player.addAttachment(plugin, "cp.afk", true);
        player.performCommand("afk");

        String displayName = PlainTextComponentSerializer.plainText().serialize(player.playerListName());
        assertEquals("TestPlayer AFK", displayName);

        // Command cooldown bypass for the next toggle
        player.addAttachment(plugin, "cp.afk.bypasscooldown", true);

        player.performCommand("afk");
        String restoredName = PlainTextComponentSerializer.plainText().serialize(player.playerListName());
        assertEquals("TestPlayer", restoredName);
    }

    @Test
    void testAfkReason() {
        player.addAttachment(plugin, "cp.afk", true);
        player.addAttachment(plugin, "cp.afk.reason", true);

        player.performCommand("afk Need to eat");
        String displayName = PlainTextComponentSerializer.plainText().serialize(player.playerListName());
        assertEquals("TestPlayer AFK", displayName);
    }

    @Test
    void testAfkReasonLimit() {
        player.addAttachment(plugin, "cp.afk", true);
        player.addAttachment(plugin, "cp.afk.reason", true);

        String longReason = "A".repeat(100);
        player.performCommand("afk " + longReason);

        String serialized = LegacyComponentSerializer.legacySection().serialize(player.nextComponentMessage());
        assertEquals("§cThe reason is too long", serialized);
    }
}
