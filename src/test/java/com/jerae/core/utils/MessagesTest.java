package com.jerae.core.utils;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class MessagesTest {

    private ServerMock server;
    private CorePlugin plugin;
    private Messages messages;

    @BeforeEach
    void setUp() throws IOException {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);

        File configFile = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("test-message", "&aTest");
        config.save(configFile);

        messages = new Messages(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testCache() {
        Component first = messages.get("test-message");
        Component second = messages.get("test-message");

        // Before implementation, these will likely be different instances
        // After implementation, they should be the same instance
        assertSame(first, second, "Subsequent calls for the same key should return the same instance");
    }

    @Test
    void testReloadClearsCache() {
        Component first = messages.get("test-message");
        messages.reload();
        Component second = messages.get("test-message");

        assertNotSame(first, second, "Reloading should clear the cache and return a new instance");
    }
}
