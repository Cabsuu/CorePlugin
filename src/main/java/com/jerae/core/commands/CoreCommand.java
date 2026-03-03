package com.jerae.core.commands;

import com.jerae.core.CorePlugin;
import com.jerae.core.utils.ConfigUpdater;
import com.jerae.core.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CoreCommand implements CommandExecutor {

    private final CorePlugin plugin;
    private final Messages messages;

    public CoreCommand(CorePlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("cp.reload")) {
                sender.sendMessage(messages.get("no-permission"));
                return true;
            }

            ConfigUpdater.updateConfig(plugin, "config.yml");
            ConfigUpdater.updateConfig(plugin, "messages.yml");

            plugin.reloadConfig();
            messages.reload();

            sender.sendMessage(messages.get("reload-success"));
            return true;
        }

        sender.sendMessage("Usage: /cp reload");
        return true;
    }
}
