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

        Messages messages = new Messages(this);
        if (getCommand("nickname") != null) {
            getCommand("nickname").setExecutor(new NickCommand(this, messages));
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
