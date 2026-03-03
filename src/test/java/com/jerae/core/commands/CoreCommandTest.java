package com.jerae.core.commands;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.jerae.core.CorePlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoreCommandTest {

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
        player.performCommand("cp reload");
        player.assertSaid("§cYou do not have permission to use this command.");
    }

    @Test
    void testReload() {
        player.addAttachment(plugin, "cp.reload", true);
        player.performCommand("cp reload");
        player.assertSaid("§aCorePlugin configuration reloaded.");
    }

    @Test
    void testUsage() {
        player.addAttachment(plugin, "cp.reload", true);
        player.performCommand("cp");
        player.assertSaid("Usage: /cp reload");
    }
}
