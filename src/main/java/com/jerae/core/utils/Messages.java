package com.jerae.core.utils;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Messages {

    private final CorePlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private final Map<String, Component> cache = new ConcurrentHashMap<>();

    public Messages(CorePlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    public void reload() {
        cache.clear();
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
        return cache.computeIfAbsent(key, k -> {
            String message = config.getString(k);
            if (message == null) {
                return Component.text("Message not found: " + k);
            }
            // Translate message with all options true since it's the server message
            String translated = ColorUtil.translate(message, true, true, true, true);
            return LegacyComponentSerializer.legacySection().deserialize(translated);
        });
    }

    public Component get(String key, String targetName, String targetDisplayName) {
        String message = config.getString(key);
        if (message == null) {
            return Component.text("Message not found: " + key);
        }

        // Translate the static template first
        String translatedTemplate = ColorUtil.translate(message, true, true, true, true);
        Component component = LegacyComponentSerializer.legacySection().deserialize(translatedTemplate);

        // Replace placeholders with un-evaluated user inputs (deserialized as legacy to preserve formatting but prevent injection)
        if (targetName != null) {
            Component replacement = LegacyComponentSerializer.legacySection().deserialize(targetName);
            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("<username>")
                    .replacement(replacement)
                    .build());
        }
        if (targetDisplayName != null) {
            Component replacement = LegacyComponentSerializer.legacySection().deserialize(targetDisplayName);
            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("<displayname>")
                    .replacement(replacement)
                    .build());
        }

        return component;
    }

    public Component get(String key, String targetName, String targetDisplayName, String afkReason, String afkCooldown) {
        Component component = get(key, targetName, targetDisplayName);

        if (afkReason != null) {
            Component replacement = LegacyComponentSerializer.legacySection().deserialize(afkReason);
            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("<afkreason>")
                    .replacement(replacement)
                    .build());
        }
        if (afkCooldown != null) {
            Component replacement = LegacyComponentSerializer.legacySection().deserialize(afkCooldown);
            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("<afkcooldown>")
                    .replacement(replacement)
                    .build());
        }

        return component;
    }
}
