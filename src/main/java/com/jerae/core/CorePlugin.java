package com.jerae.core;

import com.jerae.core.listeners.ChatListener;
import com.jerae.core.listeners.ConsoleListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ConsoleListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
