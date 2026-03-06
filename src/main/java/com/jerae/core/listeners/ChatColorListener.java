package com.jerae.core.listeners;

import com.jerae.core.CorePlugin;
import com.jerae.core.commands.ChatColorCommand;
import com.jerae.core.utils.Messages;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ChatColorListener implements Listener {

    private final CorePlugin plugin;
    private final Messages messages;

    // UUID -> "rgb" or "gradient_1" or "gradient_2:<color1>"
    private final Map<UUID, String> awaitingInput = new HashMap<>();

    private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("^&[0-9a-fA-F]$");

    public ChatColorListener(CorePlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Component title = event.getView().title();
        if (!PlainTextComponentSerializer.plainText().serialize(title).equals("Chat Colors")) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        Material type = clicked.getType();

        if (type == Material.PAPER) {
            if (!player.hasPermission("cp.chatcolor.rgb")) {
                player.sendMessage(messages.get("chatcolor-no-permission"));
                player.closeInventory();
                return;
            }
            awaitingInput.put(player.getUniqueId(), "rgb");
            player.sendMessage(messages.get("chatcolor-rgb-prompt"));
            player.closeInventory();
            return;
        }

        if (type == Material.OAK_SIGN) {
            if (!player.hasPermission("cp.chatcolor.gradient")) {
                player.sendMessage(messages.get("chatcolor-no-permission"));
                player.closeInventory();
                return;
            }
            awaitingInput.put(player.getUniqueId(), "gradient_1");
            player.sendMessage(messages.get("chatcolor-gradient-prompt-1"));
            player.closeInventory();
            return;
        }

        // Find which color was clicked
        String selectedColor = null;
        for (Map.Entry<String, Material> entry : ChatColorCommand.COLOR_MATERIALS.entrySet()) {
            if (entry.getValue() == type) {
                selectedColor = entry.getKey();
                break;
            }
        }

        if (selectedColor != null) {
            if (!selectedColor.equals("white") && !player.hasPermission("cp.chatcolor." + selectedColor)) {
                player.sendMessage(messages.get("chatcolor-no-permission"));
                player.closeInventory();
                return;
            }

            NamespacedKey key = new NamespacedKey(plugin, "chat_color");
            player.getPersistentDataContainer().set(key, PersistentDataType.STRING, "type:color,value:" + selectedColor);

            String colorCode = ChatColorCommand.COLOR_CODES.get(selectedColor);
            String finalColorName = selectedColor.replace("_", " ");

            Component msg = messages.get("chatcolor-selected")
                    .replaceText(b -> b.matchLiteral("<color>").replacement(colorCode + finalColorName));
            player.sendMessage(msg);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingInput.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        String state = awaitingInput.get(uuid);
        String input = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        NamespacedKey key = new NamespacedKey(plugin, "chat_color");

        if (state.equals("rgb")) {
            if (HEX_PATTERN.matcher(input).matches()) {
                player.getPersistentDataContainer().set(key, PersistentDataType.STRING, "type:rgb,value:" + input);

                Component msg = messages.get("chatcolor-rgb-selected")
                        .replaceText(b -> b.matchLiteral("<hexcode>").replacement("&#" + input.substring(1) + input));
                player.sendMessage(msg);
            } else {
                player.sendMessage(messages.get("chatcolor-rgb-invalid"));
            }
            awaitingInput.remove(uuid);
        } else if (state.equals("gradient_1")) {
            if (HEX_PATTERN.matcher(input).matches() || COLOR_CODE_PATTERN.matcher(input).matches()) {
                awaitingInput.put(uuid, "gradient_2:" + input);
                player.sendMessage(messages.get("chatcolor-gradient-prompt-2"));
            } else {
                player.sendMessage(messages.get("chatcolor-gradient-invalid"));
                awaitingInput.remove(uuid);
            }
        } else if (state.startsWith("gradient_2:")) {
            String color1 = state.split(":")[1];
            if (HEX_PATTERN.matcher(input).matches() || COLOR_CODE_PATTERN.matcher(input).matches()) {
                String color2 = input;
                player.getPersistentDataContainer().set(key, PersistentDataType.STRING, "type:gradient,value:" + color1 + ":" + color2);

                // We need to format color1 and color2 for display.
                // If it's hex, use it. If color code, use it.
                String display1 = formatDisplayColor(color1);
                String display2 = formatDisplayColor(color2);

                Component msg = messages.get("chatcolor-gradient-selected")
                        .replaceText(b -> b.matchLiteral("<color1>").replacement(display1))
                        .replaceText(b -> b.matchLiteral("<color2>").replacement(display2));
                player.sendMessage(msg);
            } else {
                player.sendMessage(messages.get("chatcolor-gradient-invalid"));
            }
            awaitingInput.remove(uuid);
        }
    }

    private String formatDisplayColor(String color) {
        if (color.startsWith("#")) {
            return "&#" + color.substring(1) + color;
        }
        return color + color;
    }
}
