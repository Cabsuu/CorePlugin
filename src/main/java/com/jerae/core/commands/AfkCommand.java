package com.jerae.core.commands;

import com.jerae.core.CorePlugin;
import com.jerae.core.utils.AFKManager;
import com.jerae.core.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AfkCommand implements CommandExecutor {

    private final CorePlugin plugin;
    private final Messages messages;
    private final AFKManager afkManager;

    public AfkCommand(CorePlugin plugin, Messages messages, AFKManager afkManager) {
        this.plugin = plugin;
        this.messages = messages;
        this.afkManager = afkManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cp.afk")) {
            player.sendMessage(messages.get("no-permission"));
            return true;
        }

        long cooldownRemaining = afkManager.getCooldownRemaining(player);
        if (cooldownRemaining > 0 && !player.hasPermission("cp.afk.bypasscooldown")) {
            player.sendMessage(messages.get("afk-cooldown", player.getName(), player.getName(), null, String.valueOf(cooldownRemaining)));
            return true;
        }

        String reason = null;
        if (args.length > 0) {
            if (!player.hasPermission("cp.afk.reason")) {
                player.sendMessage(messages.get("no-permission"));
                return true;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
                if (i < args.length - 1) {
                    sb.append(" ");
                }
            }
            reason = sb.toString();

            int limit = plugin.getConfig().getInt("afk-reason-character-limit", 64);
            if (reason.length() > limit && !player.hasPermission("cp.afk.bypasslimit")) {
                player.sendMessage(messages.get("afk-too-long"));
                return true;
            }
        }

        afkManager.setLastCommandUsage(player);

        if (afkManager.isAfk(player)) {
            afkManager.setAfk(player, false, null);
        } else {
            afkManager.setAfk(player, true, reason);
        }

        return true;
    }
}
