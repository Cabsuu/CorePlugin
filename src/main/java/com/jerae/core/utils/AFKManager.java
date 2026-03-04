package com.jerae.core.utils;

import com.jerae.core.CorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AFKManager {

    private final CorePlugin plugin;
    private final Messages messages;
    private final Map<UUID, Long> lastActivity;
    private final Map<UUID, Long> lastCommandUsage;
    private final Map<UUID, Boolean> isAfk;
    private final Map<UUID, Component> originalPlayerListNames;
    private BukkitTask task;

    public AFKManager(CorePlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.lastActivity = new ConcurrentHashMap<>();
        this.lastCommandUsage = new ConcurrentHashMap<>();
        this.isAfk = new ConcurrentHashMap<>();
        this.originalPlayerListNames = new ConcurrentHashMap<>();

        startTask();
    }

    private void startTask() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int autoAfkSeconds = plugin.getConfig().getInt("afk-auto", 300);
            if (autoAfkSeconds <= 0) return;

            long currentTime = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                long lastAct = lastActivity.getOrDefault(uuid, currentTime);
                if (!isAfk.getOrDefault(uuid, false) && (currentTime - lastAct > autoAfkSeconds * 1000L)) {
                    Bukkit.getScheduler().runTask(plugin, () -> setAfk(player, true, null));
                }
            }
        }, 20L, 20L); // Check every second
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
        }
    }

    public void updateActivity(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());
        if (isAfk.getOrDefault(uuid, false)) {
            setAfk(player, false, null);
        }
    }

    public void playerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.remove(uuid);
        lastCommandUsage.remove(uuid);
        isAfk.remove(uuid);
        originalPlayerListNames.remove(uuid);
    }

    public boolean isAfk(Player player) {
        return isAfk.getOrDefault(player.getUniqueId(), false);
    }

    public void setAfk(Player player, boolean afk, String reason) {
        UUID uuid = player.getUniqueId();
        isAfk.put(uuid, afk);

        if (afk) {
            originalPlayerListNames.put(uuid, player.playerListName());
            String suffix = plugin.getConfig().getString("afk-suffix", "&7&oAFK");
            Component suffixComponent = LegacyComponentSerializer.legacySection().deserialize(ColorUtil.translate(suffix, true, true, true, true));
            Component newListName = player.playerListName().append(Component.space()).append(suffixComponent);
            player.playerListName(newListName);

            if (reason != null && !reason.trim().isEmpty()) {
                Bukkit.broadcast(messages.get("afk-on-reason", player.getName(), player.getName(), reason, null));
            } else {
                Bukkit.broadcast(messages.get("afk-on", player.getName(), player.getName(), null, null));
            }
        } else {
            Component original = originalPlayerListNames.remove(uuid);
            if (original != null) {
                player.playerListName(original);
            } else {
                player.playerListName(player.name());
            }
            Bukkit.broadcast(messages.get("afk-off", player.getName(), player.getName(), null, null));
            lastActivity.put(uuid, System.currentTimeMillis());
        }
    }

    public long getCooldownRemaining(Player player) {
        UUID uuid = player.getUniqueId();
        int cooldownSeconds = plugin.getConfig().getInt("afk-cooldown", 30);
        long lastUsage = lastCommandUsage.getOrDefault(uuid, 0L);
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUsage;
        if (elapsed < cooldownSeconds * 1000L) {
            return (cooldownSeconds * 1000L - elapsed) / 1000L;
        }
        return 0L;
    }

    public void setLastCommandUsage(Player player) {
        lastCommandUsage.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
