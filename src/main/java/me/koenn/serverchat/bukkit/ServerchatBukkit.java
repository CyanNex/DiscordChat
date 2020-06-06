package me.koenn.serverchat.bukkit;

import me.koenn.serverchat.api.ServerchatAPI;
import me.koenn.serverchat.api.util.MessageCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class ServerchatBukkit extends JavaPlugin implements Listener, MessageCallback {

    public ServerchatAPI api;

    @Override
    public void onEnable() {
        PluginDescriptionFile description = this.getDescription();
        this.getLogger().info(String.format("Loading %s v%s for Spigot %s",
                description.getName(), description.getVersion(),
                Bukkit.getServer().getBukkitVersion()
        ));

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        if (!(new File(this.getDataFolder(), "config.yml")).exists()) {
            this.saveDefaultConfig();
        }

        this.api = new ServerchatAPI(new ConfigManager(this.getConfig()), this.getLogger(), this);
        this.api.init();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this.api.getMessageThread(), 0, 1);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> this.api.updatePlayerCount(Bukkit.getOnlinePlayers().size()), 0, 50);
    }

    @Override
    public void onDisable() {
        this.api.disable();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        this.api.playerChat(player.getName(), player.getUniqueId(), event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        String deathMessage = event.getDeathMessage();
        if (deathMessage == null) {
            deathMessage = String.format("%s died", event.getEntity().getName());
        }
        this.api.playerDeath(deathMessage);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        this.api.playerJoin(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        this.api.playerQuit(event.getPlayer().getName());
    }

    @Override
    public void message(@NotNull String message) {
        Bukkit.broadcastMessage(message);
    }
}
