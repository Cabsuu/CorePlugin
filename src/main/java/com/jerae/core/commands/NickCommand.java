package com.jerae.core.commands;

import com.jerae.core.CorePlugin;
import com.jerae.core.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NickCommand implements CommandExecutor {

    private final CorePlugin plugin;
    private final Messages messages;

    public NickCommand(CorePlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("cp.nick")) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /nickname [player] [-reset] <name>");
            return true;
        }

        Player target = null;
        boolean reset = false;
        String newName = null;

        int argIndex = 0;

        // Try to parse [player]
        if (args.length > 1) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p != null) {
                target = p;
                argIndex = 1;
            }
        }

        // Try to parse [-reset]
        if (argIndex < args.length && (args[argIndex].equalsIgnoreCase("-reset") || args[argIndex].equalsIgnoreCase("-r"))) {
            reset = true;
            argIndex++;
        }

        // Parse <name>
        if (argIndex < args.length) {
            StringBuilder sb = new StringBuilder();
            for (int i = argIndex; i < args.length; i++) {
                sb.append(args[i]);
                if (i < args.length - 1) {
                    sb.append(" ");
                }
            }
            newName = sb.toString();
        }

        // Handle case where [player] was accidentally parsed as <name> because there was no <name>
        // Example: `/nick Bob` when there is a player named Bob. It should change MY nickname to "Bob".
        // But what if it's `/nick Bob -reset`? It should reset Bob's nickname.
        // What if it's `/nick Bob NewName`? It should change Bob's nickname to "NewName".
        if (target != null && newName == null && !reset) {
            // No reset flag, and no new name provided after the target.
            // This means the "target" was actually the intended new name for the sender.
            newName = args[0];
            target = null;
        }

        // Default to sender if no target was explicitly given
        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must specify a player from the console.");
                return true;
            }
            target = (Player) sender;
        }

        if (target != sender && !sender.hasPermission("cp.nick.others")) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }

        if (!reset && newName == null) {
            sender.sendMessage("Usage: /nickname [player] [-reset] <name>");
            return true;
        }

        if (reset) {
            target.displayName(net.kyori.adventure.text.Component.text(target.getName()));
            target.getPersistentDataContainer().remove(new org.bukkit.NamespacedKey(plugin, "nickname"));
            if (target == sender) {
                sender.sendMessage(messages.get("nick-reset"));
            } else {
                sender.sendMessage(messages.get("nick-reset-other", target.getName(), target.getName()));
            }
        } else {
            // Apply color permissions if sender is a player, else console has all
            boolean color = sender.hasPermission("cp.nick.color") || !(sender instanceof Player);
            boolean format = sender.hasPermission("cp.nick.format") || !(sender instanceof Player);
            boolean rgb = sender.hasPermission("cp.nick.rgb") || !(sender instanceof Player);
            boolean gradient = sender.hasPermission("cp.nick.gradient") || !(sender instanceof Player);

            // Validation
            String translatedTemp = com.jerae.core.utils.ColorUtil.translate(newName, color, format, rgb, gradient);
            String stripped = net.md_5.bungee.api.ChatColor.stripColor(translatedTemp);

            // Check alphanumeric + underscore
            if (!stripped.matches("^[a-zA-Z0-9_]+$")) {
                sender.sendMessage(messages.get("invalid-characters"));
                return true;
            }

            // Check length
            boolean bypassLimit = sender.hasPermission("cp.nick.bypasslimit");
            if (!bypassLimit) {
                int limit = plugin.getConfig().getInt("nick-character-limit", 16);
                boolean ignoreColorCodes = plugin.getConfig().getBoolean("ignore-color-codes", false);

                int lengthToCheck = ignoreColorCodes ? stripped.length() : newName.length();
                if (lengthToCheck > limit) {
                    if (target == sender) {
                        sender.sendMessage(messages.get("nick-too-long"));
                    } else {
                        sender.sendMessage(messages.get("nick-too-long-other"));
                    }
                    return true;
                }
            }

            String prefix = "";
            if (!target.hasPermission("cp.nick.hideprefix")) {
                prefix = plugin.getConfig().getString("nickname-prefix", "*");
            }

            String finalName = prefix + newName;
            String finalTranslated = com.jerae.core.utils.ColorUtil.translate(finalName, color, format, rgb, gradient);

            net.kyori.adventure.text.Component displayComponent = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(finalTranslated);
            target.displayName(displayComponent);
            target.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "nickname"), org.bukkit.persistence.PersistentDataType.STRING, finalTranslated);

            if (target == sender) {
                sender.sendMessage(messages.get("nick-changed", target.getName(), finalTranslated));
            } else {
                sender.sendMessage(messages.get("nick-changed-other", target.getName(), finalTranslated));
            }
        }

        return true;
    }
}
