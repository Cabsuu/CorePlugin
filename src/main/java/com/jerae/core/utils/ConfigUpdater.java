package com.jerae.core.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigUpdater {

    public static void updateConfig(Plugin plugin, String resourceName) {
        File file = new File(plugin.getDataFolder(), resourceName);
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
            return;
        }

        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(resourceName);
        if (defaultStream == null) {
            return;
        }

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
        boolean missingKeys = false;

        for (String key : defaultConfig.getKeys(true)) {
            if (!currentConfig.contains(key)) {
                missingKeys = true;
                break;
            }
        }

        if (missingKeys) {
            plugin.getLogger().info("Updating " + resourceName + " because of missing keys.");
            File oldFile = new File(plugin.getDataFolder(), resourceName + ".old");
            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);

            plugin.saveResource(resourceName, false);
            File newFile = new File(plugin.getDataFolder(), resourceName);
            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(newFile);

            // Copy old values to new config
            for (String key : currentConfig.getKeys(true)) {
                if (newConfig.contains(key) && !currentConfig.isConfigurationSection(key)) {
                    newConfig.set(key, currentConfig.get(key));
                }
            }

            try {
                newConfig.save(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
