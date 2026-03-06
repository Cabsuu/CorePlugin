package com.jerae.core.commands;

import com.jerae.core.CorePlugin;
import com.jerae.core.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class ChatColorCommand implements CommandExecutor {

    private final CorePlugin plugin;
    private final Messages messages;

    // Map of permission name -> Material (wool color)
    public static final Map<String, Material> COLOR_MATERIALS = new LinkedHashMap<>();

    static {
        COLOR_MATERIALS.put("black", Material.BLACK_WOOL);
        COLOR_MATERIALS.put("dark_blue", Material.BLUE_WOOL);
        COLOR_MATERIALS.put("dark_green", Material.GREEN_WOOL);
        COLOR_MATERIALS.put("dark_aqua", Material.CYAN_WOOL);
        COLOR_MATERIALS.put("dark_red", Material.RED_WOOL);
        COLOR_MATERIALS.put("dark_purple", Material.PURPLE_WOOL);
        COLOR_MATERIALS.put("gold", Material.ORANGE_WOOL);
        COLOR_MATERIALS.put("gray", Material.LIGHT_GRAY_WOOL);
        COLOR_MATERIALS.put("dark_gray", Material.GRAY_WOOL);
        COLOR_MATERIALS.put("blue", Material.LIGHT_BLUE_WOOL);
        COLOR_MATERIALS.put("green", Material.LIME_WOOL);
        COLOR_MATERIALS.put("aqua", Material.CYAN_WOOL); // Minecraft mapping differs slightly
        COLOR_MATERIALS.put("red", Material.RED_WOOL);
        COLOR_MATERIALS.put("light_purple", Material.MAGENTA_WOOL);
        COLOR_MATERIALS.put("yellow", Material.YELLOW_WOOL);
        COLOR_MATERIALS.put("white", Material.WHITE_WOOL);
    }

    public static final Map<String, String> COLOR_CODES = new LinkedHashMap<>();
    static {
        COLOR_CODES.put("black", "&0");
        COLOR_CODES.put("dark_blue", "&1");
        COLOR_CODES.put("dark_green", "&2");
        COLOR_CODES.put("dark_aqua", "&3");
        COLOR_CODES.put("dark_red", "&4");
        COLOR_CODES.put("dark_purple", "&5");
        COLOR_CODES.put("gold", "&6");
        COLOR_CODES.put("gray", "&7");
        COLOR_CODES.put("dark_gray", "&8");
        COLOR_CODES.put("blue", "&9");
        COLOR_CODES.put("green", "&a");
        COLOR_CODES.put("aqua", "&b");
        COLOR_CODES.put("red", "&c");
        COLOR_CODES.put("light_purple", "&d");
        COLOR_CODES.put("yellow", "&e");
        COLOR_CODES.put("white", "&f");
    }

    public ChatColorCommand(CorePlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage(messages.get("chatcolor-gui-open"));

        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Chat Colors"));

        NamespacedKey key = new NamespacedKey(plugin, "chat_color");
        String currentConfig = player.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (currentConfig == null) {
            currentConfig = "type:color,value:white"; // default
        }

        int slot = 0;
        for (Map.Entry<String, Material> entry : COLOR_MATERIALS.entrySet()) {
            String colorName = entry.getKey();
            Material material = entry.getValue();

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(colorName.replace("_", " "), NamedTextColor.WHITE));

            // Check if this is the currently selected color
            if (currentConfig.equals("type:color,value:" + colorName)) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // RGB
        ItemStack rgbItem = new ItemStack(Material.PAPER);
        ItemMeta rgbMeta = rgbItem.getItemMeta();
        rgbMeta.displayName(Component.text("RGB Hex Color", NamedTextColor.YELLOW));
        if (currentConfig.startsWith("type:rgb")) {
            rgbMeta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            rgbMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        rgbItem.setItemMeta(rgbMeta);
        gui.setItem(18, rgbItem);

        // Gradient
        ItemStack gradientItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta gradientMeta = gradientItem.getItemMeta();
        gradientMeta.displayName(Component.text("Gradient Color", NamedTextColor.GOLD));
        if (currentConfig.startsWith("type:gradient")) {
            gradientMeta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            gradientMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        gradientItem.setItemMeta(gradientMeta);
        gui.setItem(19, gradientItem);

        player.openInventory(gui);

        return true;
    }
}
