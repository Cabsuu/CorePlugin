package com.jerae.core;

import com.jerae.core.commands.CoreCommand;
import com.jerae.core.commands.NickCommand;
import com.jerae.core.listeners.ChatListener;
import com.jerae.core.listeners.ConsoleListener;
import com.jerae.core.utils.ConfigUpdater;
import com.jerae.core.utils.Messages;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ConfigUpdater.updateConfig(this, "config.yml");
        ConfigUpdater.updateConfig(this, "messages.yml");

        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ConsoleListener(), this);
        getServer().getPluginManager().registerEvents(new com.jerae.core.listeners.PlayerJoinListener(this), this);

        Messages messages = new Messages(this);
        com.jerae.core.utils.AFKManager afkManager = new com.jerae.core.utils.AFKManager(this, messages);
        getServer().getPluginManager().registerEvents(new com.jerae.core.listeners.PlayerActivityListener(afkManager), this);

        if (getCommand("nickname") != null) {
            getCommand("nickname").setExecutor(new NickCommand(this, messages));
        }
        if (getCommand("afk") != null) {
            getCommand("afk").setExecutor(new com.jerae.core.commands.AfkCommand(this, messages, afkManager));
        }
        if (getCommand("cp") != null) {
            getCommand("cp").setExecutor(new CoreCommand(this, messages));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
