package com.jerae.core.listeners;

import com.jerae.core.CorePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.event.server.ServerCommandEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ConsoleListenerTest {

    private ServerMock server;
    private CorePlugin plugin;
    private ConsoleListener listener;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
        listener = new ConsoleListener();
        server.getPluginManager().registerEvents(listener, plugin);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testSayCommandTranslation() {
        CommandSender sender = server.getConsoleSender();
        String originalCommand = "say &aHello";
        ServerCommandEvent event = new ServerCommandEvent(sender, originalCommand);

        listener.onServerCommand(event);

        assertEquals("say §aHello", event.getCommand());
    }

    @Test
    void testOtherCommandIgnored() {
        CommandSender sender = server.getConsoleSender();
        String originalCommand = "stop";
        ServerCommandEvent event = new ServerCommandEvent(sender, originalCommand);

        listener.onServerCommand(event);

        assertEquals("stop", event.getCommand());
    }

    @Test
    void testSayCommandWithGradient() {
        CommandSender sender = server.getConsoleSender();
        String input = "say <#000000:#FFFFFF>Test";
        ServerCommandEvent event = new ServerCommandEvent(sender, input);

        listener.onServerCommand(event);

        assertNotEquals(input, event.getCommand());
        // Should start with say and contain §x
        assert(event.getCommand().startsWith("say "));
        assert(event.getCommand().contains("§x"));
    }
}
