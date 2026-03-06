package com.jerae.core.commands;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatColorCommandTest {

    private ServerMock server;
    private CorePlugin plugin;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CorePlugin.class);
        player = server.addPlayer();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testChatColorCommandOpensGUI() {
        player.performCommand("chatcolor");

        InventoryView view = player.getOpenInventory();
        assertNotNull(view);
        String title = LegacyComponentSerializer.legacySection().serialize(view.title());
        assertEquals("Chat Colors", title);

        // Assert inventory size
        assertEquals(27, view.getTopInventory().getSize());

        // Assert some items
        assertNotNull(view.getTopInventory().getItem(0));
    }
}
