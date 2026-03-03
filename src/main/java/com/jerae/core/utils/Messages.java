package com.jerae.core.utils;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Messages {

    private final CorePlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public Messages(CorePlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    public void reload() {
        if (!configFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
    }

    public Component get(String key) {
        String message = config.getString(key);
        if (message == null) {
            return Component.text("Message not found: " + key);
        }
        // Translate message with all options true since it's the server message
        String translated = ColorUtil.translate(message, true, true, true, true);
        return LegacyComponentSerializer.legacySection().deserialize(translated);
    }

    public Component get(String key, String targetName, String targetDisplayName) {
        String message = config.getString(key);
        if (message == null) {
            return Component.text("Message not found: " + key);
        }
        if (targetName != null) {
            message = message.replace("<username>", targetName);
        }
        if (targetDisplayName != null) {
            message = message.replace("<displayname>", targetDisplayName);
        }
        String translated = ColorUtil.translate(message, true, true, true, true);
        return LegacyComponentSerializer.legacySection().deserialize(translated);
    }
}
