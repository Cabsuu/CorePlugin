package com.jerae.core.utils;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesSecurityTest {

    private ServerMock server;
    private CorePlugin plugin;
    private Messages messages;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
        messages = new Messages(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testVulnerability() {
        // The message in messages.yml is: nick-changed: "&aYour nickname is now <displayname>&a."
        // If we pass a gradient tag as targetDisplayName, it shouldn't be parsed if we fix it.
        // But currently it WILL be parsed.

        String maliciousInput = "<#ff0000:#00ff00>Injected";
        Component result = messages.get("nick-changed", "TestPlayer", maliciousInput);

        String plainText = PlainTextComponentSerializer.plainText().serialize(result);

        // If it was parsed as a gradient, it should contain the word "Injected"
        // and the plain text shouldn't contain the literal tag "<#ff0000:#00ff00>"
        // because ColorUtil removes the tag.

        System.out.println("Result plain text: " + plainText);

        // In the vulnerable version, the tag is consumed by ColorUtil.
        // We want this test to FAIL if the vulnerability is present.
        // If the vulnerability is present, the tag is NOT in the plain text.
        assertTrue(plainText.contains("<#ff0000:#00ff00>"), "Vulnerability exists: Gradient tag was parsed from user input!");
    }
}
