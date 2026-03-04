package com.jerae.core.commands;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NickCommandTest {

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
        player.performCommand("nick NewName");
        player.assertSaid("§cYou do not have permission to use this command.");
    }

    @Test
    void testChangeOwnNickname() {
        player.addAttachment(plugin, "cp.nick", true);
        player.performCommand("nick NewName");

        String displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        assertEquals("*NewName", displayName);

        String serialized = LegacyComponentSerializer.legacySection().serialize(player.nextComponentMessage());
        assertEquals("§aYour nickname is now *NewName.", serialized);
    }

    @Test
    void testChangeOtherNickname() {
        PlayerMock target = server.addPlayer("TargetPlayer");
        player.addAttachment(plugin, "cp.nick", true);
        player.addAttachment(plugin, "cp.nick.others", true);

        player.performCommand("nick TargetPlayer OtherName");

        String displayName = PlainTextComponentSerializer.plainText().serialize(target.displayName());
        assertEquals("*OtherName", displayName);

        String serialized = LegacyComponentSerializer.legacySection().serialize(player.nextComponentMessage());
        assertEquals("§aTargetPlayer's nickname is now *OtherName.", serialized);
    }

    @Test
    void testInvalidCharacters() {
        player.addAttachment(plugin, "cp.nick", true);
        player.performCommand("nick !@#$%");
        player.assertSaid("§cYou can only change your nickname with letters and numbers.");
    }

    @Test
    void testNickTooLong() {
        player.addAttachment(plugin, "cp.nick", true);
        player.performCommand("nick ThisNameIsWayTooLongToBeValid123");
        player.assertSaid("§cYour nickname is too long.");
    }

    @Test
    void testBypassLimit() {
        player.addAttachment(plugin, "cp.nick", true);
        player.addAttachment(plugin, "cp.nick.bypasslimit", true);
        player.performCommand("nick ThisNameIsWayTooLongToBeValid123");

        String displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        assertEquals("*ThisNameIsWayTooLongToBeValid123", displayName);
    }

    @Test
    void testResetOwnNickname() {
        player.addAttachment(plugin, "cp.nick", true);
        player.performCommand("nick NewName");
        player.nextComponentMessage(); // clear previous message

        player.performCommand("nick -reset");
        String displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        assertEquals("TestPlayer", displayName);
        player.assertSaid("§eYou no longer have a nickname.");
    }

    @Test
    void testHidePrefix() {
        player.addAttachment(plugin, "cp.nick", true);
        player.addAttachment(plugin, "cp.nick.hideprefix", true);
        player.performCommand("nick NewName");

        String displayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        assertEquals("NewName", displayName);
    }

    @Test
    void testConsoleNoTarget() {
        server.execute("nick", server.getConsoleSender(), "NewName");
        server.getConsoleSender().assertSaid("You must specify a player from the console.");
    }
}
